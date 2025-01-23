import { FilterQueryOptions } from '@/core/dto';
import { ChatEntity } from '@/entities/ChatEntity';
import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  Logger,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { FindOptionsWhere, In, Repository } from 'typeorm';
import { UserDto } from '../user/dto';
import { getPageableData } from '@/core/db-extension';
import { ChatMapper } from './mapper';
import { CHAT_RELATIONS, MESSAGE_RELATIONS } from '@/core/defs';
import { MessageEntity } from '@/entities/MessageEntity';
import { MessageMapper } from '../message/mapper';
import { SocketService } from '@/core/socket/socket.service';
import { error_chat_not_found } from '@shared/errors';
import { ConfigService } from '@/core/config/config.service';

@Injectable()
export class ChatService {
  private logger: Logger;

  constructor(
    @InjectRepository(ChatEntity)
    private readonly repo: Repository<ChatEntity>,
    @InjectRepository(MessageEntity)
    private readonly messageRepo: Repository<MessageEntity>,
    private readonly socketService: SocketService,
    private readonly configService: ConfigService,
  ) {
    this.logger = new Logger(ChatService.name);
  }

  async filter(options: FilterQueryOptions, currentUser: UserDto) {
    this.logger.log(`${ChatService.name} filter start`);
    return getPageableData(
      this.repo,
      options,
      async (items: ChatEntity[]) => {
        if (!items.length) return [];
        const msgs = await this.messageRepo
          .createQueryBuilder('msg')
          .select('msg.id')
          .distinctOn(['chat.id'])
          .innerJoin('msg.chat', 'chat')
          .where('chat.id in(' + items.map((v) => `'${v.id}'`).join(',') + ')')
          .orderBy('chat.id', 'DESC')
          .addOrderBy('msg.createdAt', 'DESC')
          .getMany();
        const messages = await this.messageRepo.find({
          where: {
            id: In(msgs.map((v) => v.id)),
          },
          relations: MESSAGE_RELATIONS,
        });
        const result = [];
        for (const message of messages) {
          result.push({
            ...ChatMapper.map(message.chat),
            ...{
              message: MessageMapper.map(message, false),
            },
            ...(await this.getInfo(message.chat.id, currentUser.id)),
          });
        }
        return result;
      },
      CHAT_RELATIONS,
      <FindOptionsWhere<ChatEntity[]>>[
        {
          user1: { id: currentUser.id },
        },
        {
          user2: { id: currentUser.id },
        },
      ],
    );
  }

  async deleteById(id: string, currentUser: UserDto) {
    this.logger.log(`${ChatService.name} deleteById start`);
    const chat = await this.repo.findOne({
      where: {
        id,
      },
      relations: CHAT_RELATIONS,
    });
    if (!chat) throw new BadRequestException(error_chat_not_found);
    if (chat.user1.id != currentUser.id && chat.user2.id != currentUser.id)
      throw new ForbiddenException();
    const uid = chat.user1.id == currentUser.id ? chat.user2.id : chat.user1.id;
    await this.repo.remove(chat);
    await this.socketService.notifyChatRemoved(id, uid);
    this.logger.log(`${ChatService.name} deleteById end`);
    return ChatMapper.map(chat);
  }

  async getById(id: string, currentUser: UserDto) {
    this.logger.log(`${ChatService.name} getById start`);
    const chat = await this.repo.findOne({
      where: { id },
      relations: CHAT_RELATIONS,
    });
    if (!chat) throw new BadRequestException(error_chat_not_found);
    if (chat.user1.id != currentUser.id && chat.user2.id != currentUser.id)
      throw new ForbiddenException();
    const message = await this.messageRepo.findOne({
      where: {
        chat: { id },
      },
      relations: MESSAGE_RELATIONS,
      order: {
        createdAt: 'DESC',
      },
    });
    this.logger.log(`${ChatService.name} getById end`);
    return {
      ...ChatMapper.map(chat),
      ...{
        message: MessageMapper.map(message, false),
      },
      ...(await this.getInfo(id, currentUser.id)),
    };
  }

  async getInfo(id: string, userId: string) {
    this.logger.log(`${ChatService.name} getInfo start`);
    const result: any = {};
    const unreadCount = await this.messageRepo.count({
      where: {
        chat: { id },
        to: { id: userId },
        isRead: false,
      },
    });
    result.unreadCount = unreadCount;
    this.logger.log(`${ChatService.name} getInfo end`);
    return result;
  }
}
