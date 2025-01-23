import {
  Body,
  Controller,
  Delete,
  Param,
  ParseUUIDPipe,
  Post,
  UploadedFiles,
  UseInterceptors,
} from '@nestjs/common';
import { GalleryService } from './gallery.service';
import { FilesInterceptor } from '@nestjs/platform-express';
import { GalleryUploadDto } from './dto';
import { User } from '@/core/decorators/user.decorator';
import { UserDto } from '../user/dto';
import { FilterQueryOptions } from '@/core/dto';

@Controller({
  path: 'gallery',
})
export class GalleryController {
  constructor(private readonly service: GalleryService) {}

  @Post('upload')
  @UseInterceptors(FilesInterceptor('files'))
  async upload(
    @Body() dto: GalleryUploadDto,
    @User() user: UserDto,
    @UploadedFiles() files: Express.Multer.File[],
  ) {
    return this.service.upload(dto, user, files);
  }

  @Post('filter/:serviceId')
  async filter(
    @Body() options: FilterQueryOptions,
    @Param('serviceId', ParseUUIDPipe) id: string,
  ) {
    return this.service.filter(options, id);
  }

  @Delete(':photo')
  async delete(@Param('photo') photo: string) {
    return this.service.delete(photo);
  }
}
