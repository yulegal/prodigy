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
  UseInterceptors,
} from '@nestjs/common';
import { ServiceService } from './service.service';
import { FileInterceptor } from '@nestjs/platform-express';
import { ServiceCreateDto, ServiceRatingDto, ServiceUpdateDto } from './dto';
import { User } from '@/core/decorators/user.decorator';
import { UserDto } from '../user/dto';
import { FilterQueryOptions } from '@/core/dto';

@Controller({
  path: 'service',
})
export class ServiceController {
  constructor(private readonly service: ServiceService) {}

  @Get('get-user-service')
  async getUserService(@User() user: UserDto) {
    return this.service.getUserService(user);
  }

  @Post()
  @UseInterceptors(FileInterceptor('file'))
  async create(
    @Body() dto: ServiceCreateDto,
    @User() user: UserDto,
    @UploadedFile() file?: Express.Multer.File,
  ) {
    return this.service.create(dto, user, file);
  }

  @Put()
  @UseInterceptors(FileInterceptor('file'))
  async update(
    @Body() dto: ServiceUpdateDto,
    @User() user: UserDto,
    @UploadedFile() file?: Express.Multer.File,
  ) {
    return this.service.update(dto, user, file);
  }

  @Get(':id')
  async getById(@Param('id', ParseUUIDPipe) id: string) {
    return this.service.getById(id);
  }

  @Delete(':id')
  async deleteById(
    @Param('id', ParseUUIDPipe) id: string,
    @User() user: UserDto,
  ) {
    return this.service.deleteById(id, user);
  }

  @Post('filter')
  async filter(@Body() options: FilterQueryOptions, @User() user?: UserDto) {
    return this.service.filter(options, user);
  }

  @Delete('remove-photo/:id')
  async deletePhoto(
    @Param('id', ParseUUIDPipe) id: string,
    @User() user: UserDto,
  ) {
    return this.service.deletePhoto(id, user);
  }

  @Post('toggle-rating')
  async toggleRating(
    @Body() dto: ServiceRatingDto,
    @User() currentUser: UserDto,
  ) {
    return this.service.toggleRating(dto, currentUser);
  }

  @Get('toggle-favorites/:id')
  async toggleFavorite(
    @Param('id', ParseUUIDPipe) id: string,
    @User() user: UserDto,
  ) {
    return this.service.toggleFavorites(id, user);
  }

  @Post('filter-favorites')
  async filterFavorites(
    @Body() options: FilterQueryOptions,
    @User() user: UserDto,
  ) {
    return this.service.filterFavorites(options, user);
  }

  @Delete('remove-from-favorites/:id')
  async removeFromFavorites(
    @Param('id', ParseUUIDPipe) id: string,
    @User() user: UserDto,
  ) {
    return this.service.removeFromFavorites(id, user);
  }
}
