import {
  Body,
  Controller,
  Delete,
  Param,
  ParseUUIDPipe,
  Post,
  Put,
  UploadedFiles,
  UseInterceptors,
} from '@nestjs/common';
import { MessageService } from './message.service';
import { FilesInterceptor } from '@nestjs/platform-express';
import { MessageCreateDto, MessageForwardDto, MessageUpdateDto } from './dto';
import { User } from '@/core/decorators/user.decorator';
import { UserDto } from '../user/dto';
import { FilterQueryOptions } from '@/core/dto';
import { BaseData } from '@shared/interfaces';

@Controller({
  path: 'message',
})
export class MessageController {
  constructor(private readonly service: MessageService) {}

  @Post()
  @UseInterceptors(FilesInterceptor('files'))
  async create(
    @Body() dto: MessageCreateDto,
    @User() user: UserDto,
    @UploadedFiles() files?: Express.Multer.File[],
  ) {
    return this.service.create(dto, user, files);
  }

  @Put()
  @UseInterceptors(FilesInterceptor('files'))
  async update(
    @Body() dto: MessageUpdateDto,
    @User() user: UserDto,
    @UploadedFiles() files?: Express.Multer.File[],
  ) {
    return this.service.update(dto, user, files);
  }

  @Post('filter/:id')
  async filter(
    @Body() options: FilterQueryOptions,
    @User() user: UserDto,
    @Param('id', ParseUUIDPipe) id: string,
  ) {
    return this.service.filter(options, user, id);
  }

  @Delete(':id')
  async deleteById(
    @Param('id', ParseUUIDPipe) id: string,
    @User() user: UserDto,
  ) {
    return this.service.deleteById(id, user);
  }

  @Post('forward')
  async forward(@Body() dto: MessageForwardDto, @User() user: UserDto) {
    return this.service.forward(dto, user);
  }

  @Post('toggle-rating')
  async toggleRating(@Body() dto: BaseData, @User() user: UserDto) {
    return this.service.toggleRating(dto, user);
  }
}
