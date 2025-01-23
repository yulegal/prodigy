import {
  Body,
  Controller,
  Get,
  Param,
  ParseUUIDPipe,
  Post,
} from '@nestjs/common';
import { NotificationService } from './notification.service';
import { FilterQueryOptions } from '@/core/dto';
import { User } from '@/core/decorators/user.decorator';
import { UserDto } from '../user/dto';
import { BroadcastCreateDto } from './dto';

@Controller({
  path: 'notification',
})
export class NotificationController {
  constructor(private readonly service: NotificationService) {}

  @Post('filter')
  async filter(@Body() options: FilterQueryOptions, @User() user: UserDto) {
    return this.service.filter(options, user);
  }

  @Get('get-count')
  async getCount(@User() user: UserDto) {
    return this.service.getCount(user);
  }

  @Get('read/:id')
  async read(@Param('id', ParseUUIDPipe) id: string, @User() user: UserDto) {
    return this.service.read(id, user);
  }

  @Post('broadcast')
  async broadcast(@Body() dto: BroadcastCreateDto, @User() user: UserDto) {
    return this.service.broadcast(dto, user);
  }
}
