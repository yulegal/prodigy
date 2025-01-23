import {
  Body,
  Controller,
  Get,
  Param,
  ParseUUIDPipe,
  Post,
} from '@nestjs/common';
import { BookingService } from './booking.service';
import { BookingCreateDto, BookingRebookDto } from './dto';
import { UserDto } from '../user/dto';
import { User } from '@/core/decorators/user.decorator';
import { FilterQueryOptions } from '@/core/dto';

@Controller({
  path: 'booking',
})
export class BookingController {
  constructor(private readonly service: BookingService) {}

  @Post()
  async create(@Body() dto: BookingCreateDto, @User() user: UserDto) {
    return this.service.create(dto, user);
  }

  @Post('rebook')
  async rebook(@Body() dto: BookingRebookDto, @User() user: UserDto) {
    return this.service.rebook(dto, user);
  }

  @Get('cancel-booking/:id')
  async cancelBooking(
    @Param('id', ParseUUIDPipe) id: string,
    @User() user: UserDto,
  ) {
    return this.service.cancelBooking(id, user);
  }

  @Get('finish-booking/:id')
  async finishBooking(
    @Param('id', ParseUUIDPipe) id: string,
    @User() user: UserDto,
  ) {
    return this.service.finishBooking(id, user);
  }

  @Post('filter')
  async filter(@Body() options: FilterQueryOptions, @User() user: UserDto) {
    return this.service.filter(options, user);
  }
}
