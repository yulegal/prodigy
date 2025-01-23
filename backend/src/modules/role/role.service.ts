import { getPageableData } from '@/core/db-extension';
import { FilterQueryOptions } from '@/core/dto';
import { RoleEntity } from '@/entities/RoleEntity';
import { Injectable, Logger } from '@nestjs/common';
import { Repository } from 'typeorm';
import { RoleMapper } from './mapper';
import { InjectRepository } from '@nestjs/typeorm';

@Injectable()
export class RoleService {
  private logger: Logger;

  constructor(
    @InjectRepository(RoleEntity)
    private readonly repository: Repository<RoleEntity>,
  ) {
    this.logger = new Logger(RoleService.name);
  }

  async filter(options: FilterQueryOptions) {
    this.logger.log(`${RoleService.name} filter`);
    return getPageableData(this.repository, options, RoleMapper.mapList);
  }
}
