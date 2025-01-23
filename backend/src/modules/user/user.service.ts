import { UserEnitity } from '@/entities/UserEntity';
import { BadRequestException, Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { FindOptionsWhere, ILike, In, Repository } from 'typeorm';
import { error_user_not_found } from '@shared/errors';
import { UserMapper } from './mapper';
import {
  CONTACT_RELATIONS,
  EVENTS,
  USER_RELATIONS,
  USER_ROLES,
} from '@/core/defs';
import { JwtService } from '@nestjs/jwt';
import {
  UserCreateDto,
  UserDto,
  UserEditNameDto,
  UserHandleContactDto,
  UserLoginDto,
} from './dto';
import {
  getFileExtensionByName,
  randomString,
  removeFile,
  saveFile,
} from '@/core/lib/utils';
import { RoleEntity } from '@/entities/RoleEntity';
import { ConfigService } from '@/core/config/config.service';
import { v4 } from 'uuid';
import { OnEvent } from '@nestjs/event-emitter';
import { SocketService } from '@/core/socket/socket.service';
import { ContactEntity } from '@/entities/ContactEntity';
import { getPageableData } from '@/core/db-extension';
import { FilterQueryOptions } from '@/core/dto';

@Injectable()
export class UserService {
  private logger: Logger;

  constructor(
    @InjectRepository(UserEnitity)
    private readonly repo: Repository<UserEnitity>,
    private readonly jwtService: JwtService,
    @InjectRepository(RoleEntity)
    private readonly roleRepo: Repository<RoleEntity>,
    private readonly configService: ConfigService,
    private readonly socketService: SocketService,
    @InjectRepository(ContactEntity)
    private readonly contactRepo: Repository<ContactEntity>,
  ) {
    this.logger = new Logger(UserService.name);
  }

  async getById(id: string) {
    this.logger.log(`${UserService.name} getById start`);
    const user = await this.repo.findOne({
      where: { id },
      relations: USER_RELATIONS,
    });
    if (!user) throw new BadRequestException(error_user_not_found);
    this.logger.log(`${UserService.name} getById end`);
    return UserMapper.map(user);
  }

  async login(dto: UserLoginDto) {
    this.logger.log(`${UserService.name} login start`);
    const user = await this.repo.findOne({
      where: { login: dto.login },
      relations: USER_RELATIONS,
    });
    const code = randomString(6, '0123456789');
    console.log('code', code);
    if (!user) throw new BadRequestException(code);
    const mapped = UserMapper.map(user);
    this.logger.log(`${UserService.name} login end`);
    return {
      accessToken: this.jwtService.sign(mapped),
      user: mapped,
      code,
    };
  }

  async create(dto: UserCreateDto, file?: Express.Multer.File) {
    this.logger.log(`${UserService.name} create start`);
    const user = new UserEnitity();
    user.name = dto.name;
    user.login = dto.login;
    user.locale = dto.locale;
    user.role = await this.roleRepo.findOne({
      where: { id: USER_ROLES.user },
    });
    if (file) {
      const ext = getFileExtensionByName(file.originalname);
      const path = this.configService.getPublicFilesPath();
      const fileName = `${v4() + Date.now()}${ext ? '.' + ext : ''}`;
      const fullPath = `${path}/${fileName}`;
      await saveFile(fullPath, file.buffer, 'binary');
      user.icon = fileName;
    }
    const result = await this.repo.save(user);
    const mapped = UserMapper.map(result);
    this.logger.log(`${UserService.name} create end`);
    return {
      accessToken: this.jwtService.sign(mapped),
      user: mapped,
    };
  }

  async editName(dto: UserEditNameDto, currentUser: UserDto) {
    this.logger.log(`${UserService.name} editName start`);
    const user = await this.repo.findOne({
      where: { id: currentUser.id },
      relations: USER_RELATIONS,
    });
    user.name = dto.name;
    const result = await this.repo.save(user);
    this.logger.log(`${UserService.name} editName end`);
    return UserMapper.map(result);
  }

  async uploadAvatar(currentUser: UserDto, file: Express.Multer.File) {
    this.logger.log(`${UserService.name} uploadAvatar start`);
    const user = await this.repo.findOne({
      where: { id: currentUser.id },
      relations: USER_RELATIONS,
    });
    if (user.icon)
      removeFile(this.configService.getPublicFilesPath() + '/' + user.icon);
    const ext = getFileExtensionByName(file.originalname);
    const path = this.configService.getPublicFilesPath();
    const fileName = `${v4() + Date.now()}${ext ? '.' + ext : ''}`;
    const fullPath = `${path}/${fileName}`;
    await saveFile(fullPath, file.buffer, 'binary');
    user.icon = fileName;
    const result = await this.repo.save(user);
    this.logger.log(`${UserService.name} uploadAvatar end`);
    return UserMapper.map(result);
  }

  async deleteAvatar(currentUser: UserDto) {
    this.logger.log(`${UserService.name} deleteAvatar start`);
    const user = await this.repo.findOne({
      where: { id: currentUser.id },
      relations: USER_RELATIONS,
    });
    removeFile(this.configService.getPublicFilesPath() + '/' + user.icon);
    user.icon = null;
    const result = await this.repo.save(user);
    this.logger.log(`${UserService.name} deleteAvatar end`);
    return UserMapper.map(result);
  }

  @OnEvent(EVENTS.CHANGE_ROLE)
  async changeRole(ids: string[], roleId: string) {
    this.logger.log(`${UserService.name} changeRole start`);
    const users = await this.repo.find({
      where: { id: In(ids) },
      relations: USER_RELATIONS,
    });
    for (const user of users) {
      user.role = await this.roleRepo.findOne({
        where: { id: roleId },
      });
    }
    const result = await this.repo.save(users);
    await this.socketService.notifyRoleChange(UserMapper.mapList(result));
    this.logger.log(`${UserService.name} changeRole end`);
  }

  async relogin(currentUser: UserDto) {
    this.logger.log(`${UserService.name} relogin start`);
    const user = await this.repo.findOne({
      where: { id: currentUser.id },
      relations: USER_RELATIONS,
    });
    const mapped = UserMapper.map(user);
    this.logger.log(`${UserService.name} relogin end`);
    return {
      accessToken: this.jwtService.sign(mapped),
      user: mapped,
    };
  }

  async findUserByLogin(login: string) {
    this.logger.log(`${UserService.name} addUserToService start`);
    const user = await this.repo.findOne({
      where: {
        login,
      },
      relations: USER_RELATIONS,
    });
    if (!user) throw new BadRequestException(error_user_not_found);
    this.logger.log(`${UserService.name} addUserToService end`);
    return UserMapper.map(user);
  }

  async addContacts(dto: UserHandleContactDto, currentUser: UserDto) {
    this.logger.log(`${UserService.name} addContacts start`);
    const users = await this.repo.find({
      where: {
        login: In(
          dto.data.map((v) =>
            v.number.startsWith('0')
              ? '+996' + v.number.substring(1)
              : v.number,
          ),
        ),
      },
      relations: USER_RELATIONS,
    });
    const contacts = await this.contactRepo.find({
      where: {
        user: { id: currentUser.id },
        contact: { id: In(users.map((v) => v.id)) },
      },
      relations: CONTACT_RELATIONS,
    });
    const ids = contacts.map((v) => v.contact.id);
    const cuser = await this.repo.findOne({
      where: { id: currentUser.id },
      relations: USER_RELATIONS,
    });
    const contactsList: ContactEntity[] = [];
    for (const user of users.filter((v) => !ids.includes(v.id))) {
      const contact = new ContactEntity();
      contact.user = cuser;
      contact.contact = user;
      contactsList.push(contact);
    }
    const result = await this.contactRepo.save(contactsList);
    this.logger.log(`${UserService.name} addContacts end`);
    return UserMapper.mapList(result.map((v) => v.contact));
  }

  async filterContacts(options: FilterQueryOptions, currentUser: UserDto) {
    this.logger.log(`${UserService.name} filterContacts start`);
    const where: FindOptionsWhere<ContactEntity> = {
      user: { id: currentUser.id },
    };
    if (options.filter?.length) {
      const name = options.filter.find((v) => v.name == 'name')?.value;
      if (typeof name == 'string') {
        where.contact = {
          name: ILike(`%${name}%`),
        };
        options.filter = options.filter.filter((v) => v.name != 'name');
      }
    }
    this.logger.log(`${UserService.name} filterContacts end`);
    return getPageableData(
      this.contactRepo,
      options,
      (items: ContactEntity[]) => items.map((v) => UserMapper.map(v.contact)),
      CONTACT_RELATIONS,
      where,
    );
  }

  @OnEvent(EVENTS.HANDLE_USER_OFFLINE)
  async handleUserOffline(user: UserDto) {
    this.logger.log(`${UserService.name} handleUserOffline start`);
    const contacts = await this.contactRepo.find({
      where: {
        user: { id: user.id },
      },
      relations: CONTACT_RELATIONS,
    });
    const ids = contacts.map((v) => v.contact.id);
    await this.socketService.notifyUserOffline(user, ids);
    this.logger.log(`${UserService.name} handleUserOffline end`);
  }

  @OnEvent(EVENTS.HANDLE_USER_ONLINE)
  async handleUserOnline(user: UserDto) {
    this.logger.log(`${UserService.name} handleUserOnline start`);
    const contacts = await this.contactRepo.find({
      where: {
        user: { id: user.id },
      },
      relations: CONTACT_RELATIONS,
    });
    const ids = contacts.map((v) => v.contact.id);
    await this.socketService.notifyUserOnline(user, ids);
    this.logger.log(`${UserService.name} handleUserOnline end`);
  }
}
