import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseUUIDPipe,
  Post,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common';
import { UserService } from './user.service';
import {
  UserCreateDto,
  UserDto,
  UserEditNameDto,
  UserHandleContactDto,
  UserLoginDto,
} from './dto';
import { FileInterceptor } from '@nestjs/platform-express';
import { User } from '@/core/decorators/user.decorator';
import { FilterQueryOptions } from '@/core/dto';

@Controller({
  path: 'user',
})
export class UserController {
  constructor(private readonly service: UserService) {}

  @Get('relogin')
  async relogin(@User() user: UserDto) {
    return this.service.relogin(user);
  }

  @Get(':id')
  async getById(@Param('id', ParseUUIDPipe) id: string) {
    return this.service.getById(id);
  }

  @Post()
  @UseInterceptors(FileInterceptor('file'))
  async create(
    @Body() dto: UserCreateDto,
    @UploadedFile() file?: Express.Multer.File,
  ) {
    return this.service.create(dto, file);
  }

  @Post('login')
  async login(@Body() dto: UserLoginDto) {
    return this.service.login(dto);
  }

  @Post('edit-name')
  async editName(@Body() dto: UserEditNameDto, @User() user: UserDto) {
    return this.service.editName(dto, user);
  }

  @Post('upload-avatar')
  @UseInterceptors(FileInterceptor('file'))
  async uploadAvatar(
    @User() user: UserDto,
    @UploadedFile() file: Express.Multer.File,
  ) {
    return this.service.uploadAvatar(user, file);
  }

  @Delete('avatar')
  async deleteAvatar(@User() user: UserDto) {
    return this.service.deleteAvatar(user);
  }

  @Get('find-user-by-login/:login')
  async findUserByLogin(@Param('login') login: string) {
    return this.service.findUserByLogin(login);
  }

  @Post('add-contacts')
  async addContact(@Body() dto: UserHandleContactDto, @User() user: UserDto) {
    return this.service.addContacts(dto, user);
  }

  @Post('filter-contacts')
  async filterContacts(
    @Body() options: FilterQueryOptions,
    @User() user: UserDto,
  ) {
    return this.service.filterContacts(options, user);
  }
}
