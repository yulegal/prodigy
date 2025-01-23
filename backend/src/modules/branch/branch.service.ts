import { BranchEntity } from '@/entities/BranchEntity';
import { UserEnitity } from '@/entities/UserEntity';
import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  Logger,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { BranchCreateDto, BranchRatingDto, BranchUpdateDto } from './dto';
import { ServiceEntity } from '@/entities/ServiceEntity';
import {
  BRANCH_RELATIONS,
  EVENTS,
  SERVICE_RELATIONS,
  USER_RELATIONS,
  USER_ROLES,
} from '@/core/defs';
import { BranchUserEntity } from '@/entities/BranchUserEntity';
import { BranchMapper } from './mapper';
import { UserDto } from '../user/dto';
import {
  error_branch_not_found,
  error_service_not_found,
  error_user_add_branch_forbidden,
} from '@shared/errors';
import { FilterQueryOptions } from '@/core/dto';
import { getPageableData } from '@/core/db-extension';
import { NotificationService } from '../notification/notification.service';
import {
  USER_ADDED_TO_BRANCH_NOTIFICATIONS,
  USER_REMOVED_FROM_BRANCH_NOTIFICATIONS,
} from '@/core/translations';
import { NotificationType } from '@shared/interfaces/notification';
import { EventEmitter2 } from '@nestjs/event-emitter';
import { BranchRatingEntity } from '@/entities/BranchRatingEntity';

@Injectable()
export class BranchService {
  private logger: Logger;

  constructor(
    @InjectRepository(BranchEntity)
    private readonly repo: Repository<BranchEntity>,
    @InjectRepository(UserEnitity)
    private readonly userRepo: Repository<UserEnitity>,
    @InjectRepository(ServiceEntity)
    private readonly serviceRepo: Repository<ServiceEntity>,
    private readonly notificationService: NotificationService,
    private readonly emitter: EventEmitter2,
    @InjectRepository(BranchRatingEntity)
    private readonly ratingRepo: Repository<BranchRatingEntity>,
  ) {
    this.logger = new Logger(BranchService.name);
  }

  async create(dto: BranchCreateDto, currentUser: UserDto) {
    this.logger.log(`${BranchService.name} create start`);
    const service = await this.serviceRepo.findOne({
      where: { id: dto.serviceId },
      relations: SERVICE_RELATIONS,
    });
    if (!service) throw new BadRequestException(error_service_not_found);
    if (service.user.id != currentUser.id) throw new ForbiddenException();
    const branch = new BranchEntity();
    branch.address = dto.address;
    branch.workSchedule = dto.workSchedule;
    branch.service = service;
    branch.unit = dto.unit;
    branch.averageSession = dto.averageSession;
    if (dto.extra) branch.extra = dto.extra;
    branch.users = [];
    for (const userId of dto.userIds) {
      const user = new BranchUserEntity();
      user.user = await this.userRepo.findOne({
        where: { id: userId },
        relations: USER_RELATIONS,
      });
      if (service.user.id != userId && user.user.role.id != USER_ROLES.user)
        throw new BadRequestException(
          error_user_add_branch_forbidden + ' ' + userId,
        );
      branch.users.push(user);
      if (service.user.id == userId) continue;
      await this.notificationService.create({
        title: USER_ADDED_TO_BRANCH_NOTIFICATIONS.title[user.user.locale],
        body: USER_ADDED_TO_BRANCH_NOTIFICATIONS.body[user.user.locale]
          .replace('%name', currentUser.name)
          .replace('%address', dto.address.address),
        type: NotificationType.USER_ADDED_TO_BRANCH,
        userId: userId,
        itemId: currentUser.id,
      });
    }
    const result = await this.repo.save(branch);
    this.emitter.emit(
      EVENTS.CHANGE_ROLE,
      dto.userIds.filter((v) => v != service.user.id),
      USER_ROLES.helper,
    );
    this.logger.log(`${BranchService.name} create end`);
    return {
      ...BranchMapper.map(result),
      ...(await this.getInfo(result.id)),
    };
  }

  async update(dto: BranchUpdateDto, currentUser: UserDto) {
    this.logger.log(`${BranchService.name} update start`);
    const branch = await this.repo.findOne({
      where: { id: dto.id },
      relations: BRANCH_RELATIONS,
    });
    if (!branch) throw new BadRequestException(error_branch_not_found);
    if (branch.service.user.id != currentUser.id)
      throw new ForbiddenException();
    branch.averageSession = dto.averageSession;
    branch.unit = dto.unit;
    branch.workSchedule = dto.workSchedule;
    branch.address = dto.address;
    if (dto.extra) branch.extra = dto.extra;
    const oldUsers = branch.users;
    branch.users = [];
    for (const userId of dto.userIds) {
      const user = new BranchUserEntity();
      user.user = await this.userRepo.findOne({
        where: { id: userId },
        relations: USER_RELATIONS,
      });
      if (
        branch.service.user.id != userId &&
        user.user.role.id != USER_ROLES.user
      )
        throw new BadRequestException(
          error_user_add_branch_forbidden + ' ' + userId,
        );
      branch.users.push(user);
      if (
        !oldUsers.some((v) => v.user.id == userId) &&
        branch.service.user.id != userId
      ) {
        await this.notificationService.create({
          title: USER_ADDED_TO_BRANCH_NOTIFICATIONS.title[user.user.locale],
          body: USER_ADDED_TO_BRANCH_NOTIFICATIONS.body[user.user.locale]
            .replace('%name', currentUser.name)
            .replace('%address', dto.address.address),
          type: NotificationType.USER_ADDED_TO_BRANCH,
          itemId: currentUser.id,
          userId: user.user.id,
        });
      }
    }
    const ids = oldUsers.map((v) => v.user.id);
    for (const user of branch.users.filter((v) => !ids.includes(v.user.id))) {
      if (user.user.id == branch.service.user.id) continue;
      await this.notificationService.create({
        title: USER_REMOVED_FROM_BRANCH_NOTIFICATIONS.title[user.user.locale],
        body: USER_REMOVED_FROM_BRANCH_NOTIFICATIONS.body[user.user.locale]
          .replace('%name', user.user.name)
          .replace('%address', dto.address.address),
        type: NotificationType.USER_REMOVED_FROM_BRANCH,
        itemId: currentUser.id,
        userId: user.user.id,
      });
    }
    branch.extra = dto.extra ?? null;
    const result = await this.repo.save(branch);
    this.logger.log(`${BranchService.name} update end`);
    return {
      ...BranchMapper.map(result),
      ...(await this.getInfo(result.id)),
    };
  }

  async getById(id: string) {
    this.logger.log(`${BranchService.name} getById start`);
    const branch = await this.repo.findOne({
      where: { id },
      relations: BRANCH_RELATIONS,
    });
    if (!branch) throw new BadRequestException(error_branch_not_found);
    this.logger.log(`${BranchService.name} getById end`);
    return {
      ...BranchMapper.map(branch),
      ...(await this.getInfo(branch.id)),
    };
  }

  async deleteById(id: string, currentUser: UserDto) {
    this.logger.log(`${BranchService.name} deleteById start`);
    const branch = await this.repo.findOne({
      where: { id },
      relations: BRANCH_RELATIONS,
    });
    if (!branch) throw new BadRequestException(error_branch_not_found);
    if (branch.service.user.id != currentUser.id)
      throw new ForbiddenException();
    await this.repo.remove(branch);
    this.logger.log(`${BranchService.name} deleteById end`);
    return BranchMapper.map(branch);
  }

  async filter(options: FilterQueryOptions) {
    this.logger.log(`${BranchService.name} filter start`);
    return getPageableData(
      this.repo,
      options,
      async (items: BranchEntity[]) => {
        const result = [];
        for (const item of items) {
          result.push({
            ...BranchMapper.map(item),
            ...(await this.getInfo(item.id)),
          });
        }
        return result;
      },
      BRANCH_RELATIONS,
    );
  }

  async getInfo(id: string) {
    this.logger.log(`${BranchService.name} getInfo start`);
    const result: any = {};
    const total = await this.ratingRepo.count({
      where: {
        branch: { id },
      },
    });
    const sum = await this.ratingRepo.sum('rating', {
      branch: { id },
    });
    result.rating = Math.round(sum / total);
    this.logger.log(`${BranchService.name} getInfo end`);
    return result;
  }

  async toggleRating(dto: BranchRatingDto, currentUser: UserDto) {
    this.logger.log(`${BranchService.name} toggleRating start`);
    let rating = await this.ratingRepo.findOne({
      where: {
        user: { id: currentUser.id },
        branch: { id: dto.id },
      },
    });
    if (!rating) {
      rating = new BranchRatingEntity();
      rating.rating = dto.rating;
      rating.user = await this.userRepo.findOne({
        where: { id: currentUser.id },
      });
      rating.branch = await this.repo.findOne({
        where: { id: dto.id },
      });
    } else {
      rating.rating = dto.rating;
    }
    await this.ratingRepo.save(rating);
    const total = await this.ratingRepo.count({
      where: {
        branch: { id: dto.id },
      },
    });
    const sum = await this.ratingRepo.sum('rating', {
      branch: { id: dto.id },
    });
    this.logger.log(`${BranchService.name} toggleRating end`);
    return Math.round(sum / total);
  }
}
