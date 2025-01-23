import { ConfigService } from '@/core/config/config.service';
import { ServiceEntity } from '@/entities/ServiceEntity';
import { UserEnitity } from '@/entities/UserEntity';
import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  Logger,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { FindOptionsWhere, Repository } from 'typeorm';
import { ServiceCreateDto, ServiceRatingDto, ServiceUpdateDto } from './dto';
import { UserDto } from '../user/dto';
import {
  EVENTS,
  FAVORITE_RELATIONS,
  SERVICE_RELATIONS,
  USER_RELATIONS,
  USER_ROLES,
} from '@/core/defs';
import { CategoryEntity } from '@/entities/CategoryEntity';
import { getFileExtensionByName, removeFile, saveFile } from '@/core/lib/utils';
import { v4 } from 'uuid';
import { ServiceMapper } from './mapper';
import { error_service_not_found } from '@shared/errors';
import { FilterQueryOptions } from '@/core/dto';
import { getPageableData } from '@/core/db-extension';
import { EventEmitter2 } from '@nestjs/event-emitter';
import moment from 'moment';
import { ServiceRatingEntity } from '@/entities/ServiceRatingEntity';
import { FavoriteEntity } from '@/entities/FavoriteEntity';

@Injectable()
export class ServiceService {
  private logger: Logger;

  constructor(
    @InjectRepository(ServiceEntity)
    private readonly repo: Repository<ServiceEntity>,
    @InjectRepository(UserEnitity)
    private readonly userRepo: Repository<UserEnitity>,
    private readonly configService: ConfigService,
    @InjectRepository(CategoryEntity)
    private readonly categoryRepo: Repository<CategoryEntity>,
    private readonly emitter: EventEmitter2,
    @InjectRepository(ServiceRatingEntity)
    private readonly ratingRepo: Repository<ServiceRatingEntity>,
    @InjectRepository(FavoriteEntity)
    private readonly favoriteRepo: Repository<FavoriteEntity>,
  ) {
    this.logger = new Logger(ServiceService.name);
  }

  async create(
    dto: ServiceCreateDto,
    currentUser: UserDto,
    file?: Express.Multer.File,
  ) {
    this.logger.log(`${ServiceService.name} create start`);
    const service = new ServiceEntity();
    service.user = await this.userRepo.findOne({
      where: { id: currentUser.id },
      relations: USER_RELATIONS,
    });
    service.name = dto.name;
    service.category = await this.categoryRepo.findOne({
      where: { id: dto.categoryId },
    });
    service.averageSession = dto.averageSession;
    service.unit = dto.unit;
    service.trialEndDate = moment().add(6, 'months').toDate();
    service.workSchedule =
      typeof dto.workSchedule == 'string'
        ? JSON.parse(dto.workSchedule)
        : dto.workSchedule;
    service.address =
      typeof dto.address == 'string' ? JSON.parse(dto.address) : dto.address;
    if (dto.extra)
      service.extra =
        typeof dto.extra == 'string' ? JSON.parse(dto.extra) : dto.extra;
    if (file) {
      const ext = getFileExtensionByName(file.originalname);
      const path = this.configService.getPublicFilesPath();
      const fileName = `${v4() + Date.now()}${ext ? '.' + ext : ''}`;
      const fullPath = `${path}/${fileName}`;
      await saveFile(fullPath, file.buffer, 'binary');
      service.icon = fileName;
    }
    const result = await this.repo.save(service);
    if (service.user.role.id != USER_ROLES.provider) {
      this.emitter.emit(
        EVENTS.CHANGE_ROLE,
        [service.user.id],
        USER_ROLES.provider,
      );
    }
    this.logger.log(`${ServiceService.name} create end`);
    return {
      ...ServiceMapper.map(result),
      ...(await this.getInfo(result.id, currentUser.id)),
    };
  }

  async update(
    dto: ServiceUpdateDto,
    currentUser: UserDto,
    file?: Express.Multer.File,
  ) {
    this.logger.log(`${ServiceService.name} update start`);
    const service = await this.repo.findOne({
      where: { id: dto.id },
      relations: SERVICE_RELATIONS,
    });
    if (!service) throw new BadRequestException(error_service_not_found);
    if (service.user.id != currentUser.id) throw new ForbiddenException();
    service.name = dto.name;
    service.address =
      typeof dto.address == 'string' ? JSON.parse(dto.address) : dto.address;
    service.workSchedule =
      typeof dto.workSchedule == 'string'
        ? JSON.parse(dto.workSchedule)
        : dto.workSchedule;
    service.averageSession = dto.averageSession;
    service.unit = dto.unit;
    service.extra =
      typeof dto.extra == 'string' ? JSON.parse(dto.extra) : dto.extra;
    if (file) {
      if (service.icon)
        removeFile(
          this.configService.getPublicFilesPath() + '/' + service.icon,
        );
      const ext = getFileExtensionByName(file.originalname);
      const path = this.configService.getPublicFilesPath();
      const fileName = `${v4() + Date.now()}${ext ? '.' + ext : ''}`;
      const fullPath = `${path}/${fileName}`;
      await saveFile(fullPath, file.buffer, 'binary');
      service.icon = fileName;
    }
    const result = await this.repo.save(service);
    this.logger.log(`${ServiceService.name} update end`);
    return {
      ...ServiceMapper.map(result),
      ...(await this.getInfo(result.id, currentUser.id)),
    };
  }

  async getById(id: string, currentUser?: UserDto) {
    this.logger.log(`${ServiceService.name} getById start`);
    const service = await this.repo.findOne({
      where: { id },
      relations: SERVICE_RELATIONS,
    });
    if (!service) throw new BadRequestException(error_service_not_found);
    this.logger.log(`${ServiceService.name} getById end`);
    return {
      ...ServiceMapper.map(service),
      ...(await this.getInfo(service.id, currentUser?.id)),
    };
  }

  async deleteById(id: string, currentUser: UserDto) {
    this.logger.log(`${ServiceService.name} deleteById start`);
    const service = await this.repo.findOne({
      where: { id },
      relations: SERVICE_RELATIONS,
    });
    if (!service) throw new BadRequestException(error_service_not_found);
    if (service.user.id != currentUser.id) throw new ForbiddenException();
    await this.repo.remove(service);
    this.logger.log(`${ServiceService.name} deleteById end`);
    return ServiceMapper.map(service);
  }

  async filter(options: FilterQueryOptions, currentUser?: UserDto) {
    this.logger.log(`${ServiceService.name} filter start`);
    return getPageableData(
      this.repo,
      options,
      async (items: ServiceEntity[]) => {
        const result = [];
        for (const item of items) {
          result.push({
            ...ServiceMapper.map(item),
            ...(await this.getInfo(item.id, currentUser?.id)),
          });
        }
        return result;
      },
      SERVICE_RELATIONS,
      <FindOptionsWhere<ServiceEntity>>{
        blocked: false,
      },
    );
  }

  async deletePhoto(id: string, currentUser: UserDto) {
    this.logger.log(`${ServiceService.name} deletePhoto start`);
    const service = await this.repo.findOne({
      where: { id },
    });
    if (!service) throw new BadRequestException(error_service_not_found);
    if (service.user.id != currentUser.id) throw new ForbiddenException();
    removeFile(this.configService.getPublicFilesPath() + '/' + service.icon);
    service.icon = null;
    const result = await this.repo.save(service);
    this.logger.log(`${ServiceService.name} deletePhoto end`);
    return {
      ...ServiceMapper.map(result),
      ...(await this.getInfo(result.id, currentUser.id)),
    };
  }

  async getUserService(currentUser: UserDto) {
    this.logger.log(`${ServiceService.name} getUserService start`);
    const service = await this.repo.findOne({
      where: {
        user: { id: currentUser.id },
      },
      relations: SERVICE_RELATIONS,
    });
    if (!service) throw new BadRequestException(error_service_not_found);
    this.logger.log(`${ServiceService.name} getUserService end`);
    return {
      ...ServiceMapper.map(service),
      ...(await this.getInfo(service.id, currentUser.id)),
    };
  }

  async getInfo(id: string, userId?: string) {
    this.logger.log(`${ServiceService.name} getInfo start`);
    const result: any = {};
    const total = await this.ratingRepo.count({
      where: {
        service: { id },
      },
    });
    const sum = await this.ratingRepo.sum('rating', {
      service: { id },
    });
    result.rating = Math.round(sum / total);
    if (userId) {
      const rated = await this.ratingRepo.exists({
        where: {
          user: { id: userId },
          service: { id },
        },
      });
      const addedToFavorites = await this.favoriteRepo.exists({
        where: {
          user: { id: userId },
          service: { id },
        },
      });
      result.rated = rated;
      result.addedToFavorites = addedToFavorites;
    }
    this.logger.log(`${ServiceService.name} getInfo end`);
    return result;
  }

  async toggleRating(dto: ServiceRatingDto, currentUser: UserDto) {
    this.logger.log(`${ServiceService.name} toggleRating start`);
    let rating = await this.ratingRepo.findOne({
      where: {
        user: { id: currentUser.id },
        service: { id: dto.id },
      },
    });
    if (!rating) {
      rating = new ServiceRatingEntity();
      rating.rating = dto.rating;
      rating.user = await this.userRepo.findOne({
        where: { id: currentUser.id },
      });
      rating.service = await this.repo.findOne({
        where: { id: dto.id },
      });
    } else {
      rating.rating = dto.rating;
    }
    await this.ratingRepo.save(rating);
    const total = await this.ratingRepo.count({
      where: {
        service: { id: dto.id },
      },
    });
    const sum = await this.ratingRepo.sum('rating', {
      service: { id: dto.id },
    });
    this.logger.log(`${ServiceService.name} toggleRating end`);
    return Math.round(sum / total);
  }
  async toggleFavorites(id: string, currentUser: UserDto) {
    this.logger.log(`${ServiceService.name} toggleFavorites start`);
    let favorite = await this.favoriteRepo.findOne({
      where: {
        service: { id },
        user: { id: currentUser.id },
      },
    });
    let result;
    if (!favorite) {
      favorite = new FavoriteEntity();
      favorite.user = await this.userRepo.findOne({
        where: { id: currentUser.id },
      });
      favorite.service = await this.repo.findOne({
        where: { id },
      });
      await this.favoriteRepo.save(favorite);
      result = 1;
    } else {
      await this.favoriteRepo.remove(favorite);
      result = 0;
    }
    this.logger.log(`${ServiceService.name} toggleFavorites end`);
    return result;
  }

  async filterFavorites(options: FilterQueryOptions, currentUser: UserDto) {
    this.logger.log(`${ServiceService.name} filterFavorites start`);
    const where: FindOptionsWhere<FavoriteEntity> = {
      user: { id: currentUser.id },
      service: {
        blocked: false,
      },
    };
    return getPageableData(
      this.favoriteRepo,
      options,
      (items: FavoriteEntity[]) =>
        items.map((v) => ServiceMapper.map(v.service)),
      FAVORITE_RELATIONS,
      where,
    );
  }

  async removeFromFavorites(id: string, currentUser: UserDto) {
    this.logger.log(`${ServiceService.name} removeFromFavorites start`);
    const service = await this.favoriteRepo.findOne({
      where: {
        user: { id: currentUser.id },
        service: { id },
      },
      relations: FAVORITE_RELATIONS,
    });
    if (!service) throw new BadRequestException(error_service_not_found);
    await this.favoriteRepo.remove(service);
    this.logger.log(`${ServiceService.name} removeFromFavorites end`);
    return ServiceMapper.map(service.service);
  }
}
