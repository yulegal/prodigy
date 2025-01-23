import { BranchEntity } from '@/entities/BranchEntity';
import { GalleryEntity } from '@/entities/GalleryEntity';
import { ServiceEntity } from '@/entities/ServiceEntity';
import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  Logger,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { FindOptionsWhere, IsNull, Repository } from 'typeorm';
import { UserDto } from '../user/dto';
import { GalleryUploadDto } from './dto/index';
import { BRANCH_RELATIONS, SERVICE_RELATIONS } from '@/core/defs';
import {
  error_branch_not_found,
  error_photo_not_found,
  error_service_not_found,
} from '@shared/errors';
import { ConfigService } from '@/core/config/config.service';
import { getFileExtensionByName, removeFile, saveFile } from '@/core/lib/utils';
import { v4 } from 'uuid';
import { FilterQueryOptions } from '@/core/dto';
import { getPageableData } from '@/core/db-extension';
import { Direction } from '@shared/interfaces';

@Injectable()
export class GalleryService {
  private logger: Logger;

  constructor(
    @InjectRepository(GalleryEntity)
    private readonly repo: Repository<GalleryEntity>,
    @InjectRepository(ServiceEntity)
    private readonly serviceRepo: Repository<ServiceEntity>,
    @InjectRepository(BranchEntity)
    private readonly branchRepo: Repository<BranchEntity>,
    private readonly configService: ConfigService,
  ) {
    this.logger = new Logger(GalleryService.name);
  }

  async upload(
    dto: GalleryUploadDto,
    currentUser: UserDto,
    files: Express.Multer.File[],
  ) {
    this.logger.log(`${GalleryService.name} upload start`);
    const service = await this.serviceRepo.findOne({
      where: { id: dto.serviceId },
      relations: SERVICE_RELATIONS,
    });
    if (!service) throw new BadRequestException(error_service_not_found);
    let branch: BranchEntity | null = null;
    if (dto.branchId) {
      branch = await this.branchRepo.findOne({
        where: { id: dto.branchId },
        relations: BRANCH_RELATIONS,
      });
      if (!branch) throw new BadRequestException(error_branch_not_found);
      if (
        service.user.id != currentUser.id &&
        !branch.users.some((v) => v.user.id == currentUser.id)
      )
        throw new ForbiddenException();
    } else if (service.user.id != currentUser.id)
      throw new ForbiddenException();
    const res: GalleryEntity[] = [];
    for (const file of files) {
      const ext = getFileExtensionByName(file.originalname);
      const path = this.configService.getPublicFilesPath();
      const fileName = `${v4() + Date.now()}${ext ? '.' + ext : ''}`;
      const fullPath = `${path}/${fileName}`;
      await saveFile(fullPath, file.buffer, 'binary');
      const gallery = new GalleryEntity();
      gallery.photo = fileName;
      gallery.service = service;
      gallery.branch = branch;
      res.push(gallery);
    }
    const result = await this.repo.save(res);
    this.logger.log(`${GalleryService.name} upload end`);
    return result.map((v) => v.photo);
  }

  async filter(options: FilterQueryOptions, serviceId: string) {
    this.logger.log(`${GalleryService.name} filter start`);
    const where: FindOptionsWhere<GalleryEntity> = {
      service: { id: serviceId },
      branch: IsNull(),
    };
    if (options.filter?.length) {
      const branch = options.filter.find((v) => v.name == 'branch')?.value;
      if (branch) {
        where.branch = { id: branch as string };
        options.filter = options.filter.filter((v) => v.name != 'branch');
      }
    }
    options.sortDirection = Direction.DESC;
    options.sortField = 'createdAt';
    return getPageableData(
      this.repo,
      options,
      (items: GalleryEntity[]) => items.map((v) => v.photo),
      [],
      where,
    );
  }

  async delete(photo: string) {
    this.logger.log(`${GalleryService.name} delete start`);
    const p = await this.repo.findOne({
      where: {
        photo,
      },
    });
    if (!p) throw new BadRequestException(error_photo_not_found);
    removeFile(this.configService.getPublicFilesPath() + '/' + photo);
    await this.repo.remove(p);
    this.logger.log(`${GalleryService.name} delete end`);
  }
}
