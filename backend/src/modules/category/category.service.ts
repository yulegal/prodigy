import { CategoryEntity } from '@/entities/CategoryEntity';
import { BadRequestException, Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CategoryCreateDto, CategoryUpdateDto } from './dto';
import { getFileExtensionByName, removeFile, saveFile } from '@/core/lib/utils';
import { v4 } from 'uuid';
import { CategoryMapper } from './mapper';
import { ConfigService } from '@/core/config/config.service';
import { error_category_not_found } from '@shared/errors';
import { FilterQueryOptions } from '@/core/dto';
import { getPageableData } from '@/core/db-extension';

@Injectable()
export class CategoryService {
  private logger: Logger;

  constructor(
    @InjectRepository(CategoryEntity)
    private readonly repo: Repository<CategoryEntity>,
    private readonly configService: ConfigService,
  ) {
    this.logger = new Logger(CategoryService.name);
  }

  async create(dto: CategoryCreateDto, file?: Express.Multer.File) {
    this.logger.log(`${CategoryService.name} create start`);
    const category = new CategoryEntity();
    category.name = dto.name;
    category.type = dto.type;
    if (file) {
      const ext = getFileExtensionByName(file.originalname);
      const path = this.configService.getPublicFilesPath();
      const fileName = `${v4() + Date.now()}${ext ? '.' + ext : ''}`;
      const fullPath = `${path}/${fileName}`;
      await saveFile(fullPath, file.buffer, 'binary');
      category.icon = fileName;
    }
    const result = await this.repo.save(category);
    this.logger.log(`${CategoryService.name} create end`);
    return CategoryMapper.map(result);
  }

  async update(dto: CategoryUpdateDto, file?: Express.Multer.File) {
    this.logger.log(`${CategoryService.name} update start`);
    const category = await this.repo.findOne({
      where: { id: dto.id },
    });
    if (!category) throw new BadRequestException(error_category_not_found);
    category.name = dto.name;
    category.type = dto.type;
    if (file) {
      if (category.icon)
        removeFile(
          this.configService.getPublicFilesPath() + '/' + category.icon,
        );
      const ext = getFileExtensionByName(file.originalname);
      const path = this.configService.getPublicFilesPath();
      const fileName = `${v4() + Date.now()}${ext ? '.' + ext : ''}`;
      const fullPath = `${path}/${fileName}`;
      await saveFile(fullPath, file.buffer, 'binary');
      category.icon = fileName;
    }
    const result = await this.repo.save(category);
    this.logger.log(`${CategoryService.name} update end`);
    return CategoryMapper.map(result);
  }

  async getById(id: string) {
    this.logger.log(`${CategoryService.name} getById start`);
    const category = await this.repo.findOne({
      where: { id },
    });
    if (!category) throw new BadRequestException(error_category_not_found);
    this.logger.log(`${CategoryService.name} getById end`);
    return CategoryMapper.map(category);
  }

  async deleteById(id: string) {
    this.logger.log(`${CategoryService.name} deleteById start`);
    const category = await this.repo.findOne({
      where: { id },
    });
    if (!category) throw new BadRequestException(error_category_not_found);
    await this.repo.remove(category);
    this.logger.log(`${CategoryService.name} deleteById end`);
    return CategoryMapper.map(category);
  }

  async filter(options: FilterQueryOptions) {
    this.logger.log(`${CategoryService.name} filter start`);
    return getPageableData(this.repo, options, CategoryMapper.mapList);
  }
}
