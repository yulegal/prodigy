import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseUUIDPipe,
  Post,
} from '@nestjs/common';
import { ChatService } from './chat.service';
import { FilterQueryOptions } from '@/core/dto';
import { User } from '@/core/decorators/user.decorator';
import { UserDto } from '../user/dto';

@Controller({
  path: 'chat',
})
export class ChatController {
  constructor(private readonly service: ChatService) {}

  @Post('filter')
  async filter(@Body() options: FilterQueryOptions, @User() user: UserDto) {
    return this.service.filter(options, user);
  }

  @Delete(':id')
  async deleteById(
    @Param('id', ParseUUIDPipe) id: string,
    @User() user: UserDto,
  ) {
    return this.service.deleteById(id, user);
  }

  @Get(':id')
  async getById(@Param('id', ParseUUIDPipe) id: string, @User() user: UserDto) {
    return this.service.getById(id, user);
  }
}
