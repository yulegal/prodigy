import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseUUIDPipe,
  Post,
  Put,
} from '@nestjs/common';
import { BranchService } from './branch.service';
import { BranchCreateDto, BranchRatingDto, BranchUpdateDto } from './dto';
import { User } from '@/core/decorators/user.decorator';
import { UserDto } from '../user/dto';
import { FilterQueryOptions } from '@/core/dto';

@Controller({
  path: 'branch',
})
export class BranchController {
  constructor(private readonly service: BranchService) {}

  @Post()
  async create(@Body() dto: BranchCreateDto, @User() user: UserDto) {
    return this.service.create(dto, user);
  }

  @Put()
  async update(@Body() dto: BranchUpdateDto, @User() user: UserDto) {
    return this.service.update(dto, user);
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
  async filter(@Body() options: FilterQueryOptions) {
    return this.service.filter(options);
  }

  @Post('toggle-rating')
  async toggleRating(@Body() dto: BranchRatingDto, @User() user: UserDto) {
    return this.service.toggleRating(dto, user);
  }
}
