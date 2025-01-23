import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseUUIDPipe,
  Post,
  Put,
  UploadedFile,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { CategoryService } from './category.service';
import { FileInterceptor } from '@nestjs/platform-express';
import { Roles } from '@/core/decorators/role.decorator';
import { USER_ROLES } from '@/core/defs';
import { RolesGuard } from '@/core/guards/roles.guard';
import { CategoryCreateDto, CategoryUpdateDto } from './dto';
import { FilterQueryOptions } from '@/core/dto';

@Controller({
  path: 'category',
})
export class CategoryController {
  constructor(private readonly service: CategoryService) {}

  @Post()
  @Roles(USER_ROLES.admin)
  @UseGuards(RolesGuard)
  @UseInterceptors(FileInterceptor('file'))
  async create(
    @Body() dto: CategoryCreateDto,
    @UploadedFile() file?: Express.Multer.File,
  ) {
    return this.service.create(dto, file);
  }

  @Put()
  @Roles(USER_ROLES.admin)
  @UseGuards(RolesGuard)
  @UseInterceptors(FileInterceptor('file'))
  async update(
    @Body() dto: CategoryUpdateDto,
    @UploadedFile() file?: Express.Multer.File,
  ) {
    return this.service.update(dto, file);
  }

  @Get(':id')
  async getById(@Param('id', ParseUUIDPipe) id: string) {
    return this.service.getById(id);
  }

  @Delete(':id')
  @Roles(USER_ROLES.admin)
  @UseGuards(RolesGuard)
  async deleteById(@Param('id', ParseUUIDPipe) id: string) {
    return this.service.deleteById(id);
  }

  @Post('filter')
  async filter(@Body() options: FilterQueryOptions) {
    return this.service.filter(options);
  }
}
