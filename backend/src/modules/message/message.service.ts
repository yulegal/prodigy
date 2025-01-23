import { ConfigService } from '@/core/config/config.service';
import { MessageEntity } from '@/entities/MessageEntity';
import { UserEnitity } from '@/entities/UserEntity';
import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  Logger,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { FindOptionsWhere, Repository } from 'typeorm';
import { MessageCreateDto, MessageForwardDto, MessageUpdateDto } from './dto';
import { UserDto } from '../user/dto';
import { ChatEntity } from '@/entities/ChatEntity';
import { EVENTS, MESSAGE_RELATIONS, USER_RELATIONS } from '@/core/defs';
import { getFileExtensionByName, removeFile, saveFile } from '@/core/lib/utils';
import { v4 } from 'uuid';
import { MessageMapper } from './mapper';
import { error_empty_messages, error_message_not_found } from '@shared/errors';
import { FilterQueryOptions } from '@/core/dto';
import { getPageableData } from '@/core/db-extension';
import { OnEvent } from '@nestjs/event-emitter';
import { SocketService } from '@/core/socket/socket.service';
import { copyFileSync } from 'fs';
import { Direction } from '@shared/interfaces';

@Injectable()
export class MessageService {
  private logger: Logger;

  constructor(
    @InjectRepository(MessageEntity)
    private readonly repo: Repository<MessageEntity>,
    @InjectRepository(UserEnitity)
    private readonly userRepo: Repository<UserEnitity>,
    private readonly configService: ConfigService,
    @InjectRepository(ChatEntity)
    private readonly chatRepo: Repository<ChatEntity>,
    private readonly socketService: SocketService,
  ) {
    this.logger = new Logger(MessageService.name);
  }

  async create(
    dto: MessageCreateDto,
    currentUser: UserDto,
    files?: Express.Multer.File[],
  ) {
    this.logger.log(`${MessageService.name} create start`);
    let chat = await this.chatRepo.findOne({
      where: [
        {
          user1: { id: currentUser.id },
          user2: { id: dto.toId },
        },
        {
          user2: { id: currentUser.id },
          user1: { id: dto.toId },
        },
      ],
    });
    if (!chat) {
      chat = new ChatEntity();
      chat.user1 = await this.userRepo.findOne({
        where: { id: currentUser.id },
        relations: USER_RELATIONS,
      });
      chat.user2 = await this.userRepo.findOne({
        where: { id: dto.toId },
        relations: USER_RELATIONS,
      });
      chat = await this.chatRepo.save(chat);
    }
    const message = new MessageEntity();
    message.from = await this.userRepo.findOne({
      where: { id: currentUser.id },
      relations: USER_RELATIONS,
    });
    message.to = await this.userRepo.findOne({
      where: { id: dto.toId },
      relations: USER_RELATIONS,
    });
    message.chat = chat;
    if (dto.parentId)
      message.parent = await this.repo.findOne({
        where: { id: dto.parentId },
        relations: MESSAGE_RELATIONS,
      });
    if (dto.body) message.body = dto.body;
    if (files) {
      message.addons = [];
      for (const file of files) {
        const ext = getFileExtensionByName(file.originalname);
        const path = this.configService.getAddonsFilesPath();
        const fileName = `${v4() + Date.now()}${ext ? '.' + ext : ''}`;
        const fullPath = `${path}/${fileName}`;
        await saveFile(fullPath, file.buffer, 'binary');
        message.addons.push(fileName);
      }
    }
    const result = await this.repo.save(message);
    const mapped = MessageMapper.map(result);
    await this.socketService.notifyNewMessage(mapped);
    this.logger.log(`${MessageService.name} create end`);
    return mapped;
  }

  async update(
    dto: MessageUpdateDto,
    currentUser: UserDto,
    files?: Express.Multer.File[],
  ) {
    this.logger.log(`${MessageService.name} update start`);
    const message = await this.repo.findOne({
      where: { id: dto.id },
      relations: MESSAGE_RELATIONS,
    });
    if (!message) throw new BadRequestException(error_message_not_found);
    if (message.from.id != currentUser.id) throw new ForbiddenException();
    message.body = dto.body ?? null;
    if (files) {
      message.addons?.forEach((v) =>
        removeFile(this.configService.getAddonsFilesPath() + '/' + v),
      );
      message.addons = [];
      for (const file of files) {
        const ext = getFileExtensionByName(file.originalname);
        const path = this.configService.getAddonsFilesPath();
        const fileName = `${v4() + Date.now()}${ext ? '.' + ext : ''}`;
        const fullPath = `${path}/${fileName}`;
        await saveFile(fullPath, file.buffer, 'binary');
        message.addons.push(fileName);
      }
    }
    const result = await this.repo.save(message);
    const mapped = MessageMapper.map(result);
    await this.socketService.notifyMessageEdited(mapped);
    this.logger.log(`${MessageService.name} update end`);
    return;
  }

  async deleteById(id: string, currentUser: UserDto) {
    this.logger.log(`${MessageService.name} deleteById start`);
    const message = await this.repo.findOne({
      where: { id },
      relations: MESSAGE_RELATIONS,
    });
    if (!message) throw new BadRequestException(error_message_not_found);
    if (message.from.id != currentUser.id) throw new ForbiddenException();
    message.addons?.forEach((v) =>
      removeFile(this.configService.getAddonsFilesPath() + '/' + v),
    );
    const cid = message.chat.id;
    const uid =
      message.from.id == currentUser.id ? message.to.id : message.from.id;
    await this.repo.remove(message);
    const count = await this.repo.count({
      where: {
        chat: { id: cid },
      },
    });
    if (count) {
      await this.socketService.notifyMessageDeleted(
        id,
        message.from.id == currentUser.id ? message.to.id : message.from.id,
      );
    } else {
      const chat = await this.chatRepo.findOne({
        where: { id: cid },
      });
      await this.chatRepo.remove(chat);
      await this.socketService.notifyChatRemoved(cid, uid);
    }
    this.logger.log(`${MessageService.name} deleteById end`);
    return MessageMapper.map(message);
  }

  async filter(
    options: FilterQueryOptions,
    currentUser: UserDto,
    userId: string,
  ) {
    this.logger.log(`${MessageService.name} filter start`);
    const chat = await this.chatRepo.findOne({
      where: [
        {
          user2: { id: userId },
          user1: { id: currentUser.id },
        },
        {
          user1: { id: userId },
          user2: { id: currentUser.id },
        },
      ],
    });
    if (!chat) throw new BadRequestException(error_empty_messages);
    options.sortDirection = Direction.ASC;
    options.sortField = 'createdAt';
    return getPageableData(
      this.repo,
      options,
      MessageMapper.mapList,
      MESSAGE_RELATIONS,
      <FindOptionsWhere<MessageEntity>>{
        chat: { id: chat.id },
      },
    );
  }

  @OnEvent(EVENTS.MESSAGE_READ)
  async messageRead(id: string) {
    this.logger.log(`${MessageService.name} messageRead start`);
    const message = await this.repo.findOne({
      where: { id },
      relations: MESSAGE_RELATIONS,
    });
    message.isRead = true;
    const result = await this.repo.save(message);
    await this.socketService.notifyMessageRead(MessageMapper.map(result));
    this.logger.log(`${MessageService.name} messageRead end`);
  }

  async forward(dto: MessageForwardDto, currentUser: UserDto) {
    this.logger.log(`${MessageService.name} forward start`);
    const message = await this.repo.findOne({
      where: {
        id: dto.id,
      },
      relations: MESSAGE_RELATIONS,
    });
    if (!message) throw new BadRequestException(error_message_not_found);
    if (message.from.id != currentUser.id && message.to.id != currentUser.id)
      throw new ForbiddenException();
    let chat = await this.chatRepo.findOne({
      where: [
        {
          user1: { id: currentUser.id },
          user2: { id: dto.toId },
        },
        {
          user2: { id: currentUser.id },
          user1: { id: dto.toId },
        },
      ],
    });
    if (!chat) {
      chat = new ChatEntity();
      chat.user1 = await this.userRepo.findOne({
        where: { id: currentUser.id },
        relations: USER_RELATIONS,
      });
      chat.user2 = await this.userRepo.findOne({
        where: { id: dto.toId },
        relations: USER_RELATIONS,
      });
      chat = await this.chatRepo.save(chat);
    }
    const newMessage = new MessageEntity();
    newMessage.to = await this.userRepo.findOne({
      where: { id: dto.toId },
      relations: USER_RELATIONS,
    });
    newMessage.from = await this.userRepo.findOne({
      where: { id: currentUser.id },
      relations: USER_RELATIONS,
    });
    newMessage.body = message.body;
    newMessage.forwardedFrom =
      message.from.id == currentUser.id ? message.to : message.from;
    newMessage.chat = chat;
    newMessage.addons = [];
    message.addons?.forEach((v) => {
      const ext = getFileExtensionByName(v);
      const name = `${v4() + Date.now()}${ext ? '.' + ext : ''}`;
      copyFileSync(
        this.configService.getAddonsFilesPath() + '/' + v,
        this.configService.getAddonsFilesPath() + '/' + name,
      );
      newMessage.addons.push(name);
    });
    const result = await this.repo.save(newMessage);
    const mapped = MessageMapper.map(result);
    await this.socketService.notifyNewMessage(mapped);
    this.logger.log(`${MessageService.name} forward end`);
    return mapped;
  }
}
