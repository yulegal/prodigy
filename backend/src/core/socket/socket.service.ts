import { Injectable, Logger } from '@nestjs/common';
import { Server } from 'socket.io';
import { RedisClientService } from '../redis-client/redis-client.service';
import { NotificationDto } from '@/modules/notification/dto';
import { EVENTS, REDIS_NAMESPACE, WS_MESSAGES } from '../defs';
import { UserDto } from '@/modules/user/dto';
import { MessageDto } from '@/modules/message/dto';
import { OnEvent } from '@nestjs/event-emitter';

@Injectable()
export class SocketService {
  public socket: Server = null;
  private logger: Logger;

  constructor(private readonly redisClientService: RedisClientService) {
    this.logger = new Logger(SocketService.name);
  }

  async newNotification(notification: NotificationDto) {
    this.logger.log(`${SocketService.name} newNotification create start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const clients = allClients.filter(
      (v) => v.value.id == notification.user.id,
    );
    clients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(notification.type, JSON.stringify(notification));
    });
    this.logger.log(`${SocketService.name} newNotification create end`);
  }

  async notifyRoleChange(users: UserDto[]) {
    this.logger.log(`${SocketService.name} notifyRoleChange create start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const clients = allClients.filter((v) =>
      users.some((item) => item.id == v.value.id),
    );
    clients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(
          WS_MESSAGES.SERVER.ROLE_CHANGED,
          JSON.stringify(users.find((item) => item.id == v.value.id)),
        );
    });
    this.logger.log(`${SocketService.name} notifyRoleChange create end`);
  }

  async notifyMessageRead(message: MessageDto) {
    this.logger.log(`${SocketService.name} notifyMessageRead start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const fromClients = allClients.filter((v) => v.value.id == message.from.id);
    const toClients = allClients.filter((v) => v.value.id == message.to.id);
    fromClients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(WS_MESSAGES.SERVER.MESSAGE_READ, JSON.stringify(message));
    });
    toClients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(WS_MESSAGES.SERVER.MESSAGE_READ, JSON.stringify(message));
    });
    this.logger.log(`${SocketService.name} notifyMessageRead end`);
  }

  async notifyUserOffline(user: UserDto, userIds: string[]) {
    this.logger.log(`${SocketService.name} notifyUserOffline start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const clients = allClients.filter((v) => userIds.includes(v.value.id));
    clients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(WS_MESSAGES.SERVER.USER_OFFLINE, JSON.stringify(user));
    });
    this.logger.log(`${SocketService.name} notifyUserOffline end`);
  }

  @OnEvent(EVENTS.CHECK_USER_ONLINE)
  async checkUserOnline(id: string, clientId: string) {
    this.logger.log(`${SocketService.name} checkUserOnline start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const isOnline = allClients.some((v) => v.value.id == id);
    this.socket
      .to(clientId)
      .emit(WS_MESSAGES.SERVER.USER_ONLINE_STATUS, isOnline);
    this.logger.log(`${SocketService.name} checkUserOnline end`);
  }

  async notifyUserOnline(user: UserDto, userIds: string[]) {
    this.logger.log(`${SocketService.name} notifyUserOnline start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const clients = allClients.filter((v) => userIds.includes(v.value.id));
    clients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(WS_MESSAGES.SERVER.USER_ONLINE, JSON.stringify(user));
    });
    this.logger.log(`${SocketService.name} notifyUserOnline end`);
  }

  async notifyNewMessage(message: MessageDto) {
    this.logger.log(`${SocketService.name} notifyNewMessage start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const clients = allClients.filter((v) => v.value.id == message.to.id);
    clients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(WS_MESSAGES.SERVER.NEW_MESSAGE, JSON.stringify(message));
    });
    this.logger.log(`${SocketService.name} notifyNewMessage end`);
  }

  async notifyMessageDeleted(messageId: string, userId: string) {
    this.logger.log(`${SocketService.name} notifyMessageDeleted start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const clients = allClients.filter((v) => v.value.id == userId);
    clients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(WS_MESSAGES.SERVER.MESSAGE_DELETED, messageId);
    });
    this.logger.log(`${SocketService.name} notifyMessageDeleted end`);
  }

  async notifyChatRemoved(id: string, userId: string) {
    this.logger.log(`${SocketService.name} notifyChatRemoved start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const clients = allClients.filter((v) => v.value.id == userId);
    clients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(WS_MESSAGES.SERVER.CHAT_REMOVED, id);
    });
    this.logger.log(`${SocketService.name} notifyChatRemoved end`);
  }

  async notifyMessageEdited(message: MessageDto) {
    this.logger.log(`${SocketService.name} notifyMessageEdited start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const clients = allClients.filter((v) => v.value.id == message.to.id);
    clients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(WS_MESSAGES.SERVER.MESSAGE_EDITED, JSON.stringify(message));
    });
    this.logger.log(`${SocketService.name} notifyMessageEdited end`);
  }

  async notifyUserLoggedOut(
    userIds: string[],
    userId: string,
    clientId: string,
  ) {
    this.logger.log(`${SocketService.name} notifyUserLoggedOut start`);
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const clients = allClients.filter((v) => userIds.includes(v.value.id));
    clients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(WS_MESSAGES.SERVER.CONTACT_LOGGED_OUT, userId);
    });
    this.socket.to(clientId).emit(WS_MESSAGES.SERVER.LOGGED_OUT);
    this.logger.log(`${SocketService.name} notifyUserLoggedOut end`);
  }

  @OnEvent(EVENTS.MESSAGE_RATED)
  async messageRated(data: MessageDto, user: UserDto) {
    this.logger.log(`${SocketService.name} messageRated start`);
    const uid = data.from.id == user.id ? data.to.id : data.from.id;
    const allClients = await this.redisClientService.getAll(
      REDIS_NAMESPACE.WS + '*',
    );
    const clients = allClients.filter((v) => v.value.id == uid);
    clients.forEach((v) => {
      this.socket
        .to(v.key.substring(3))
        .emit(WS_MESSAGES.SERVER.MESSAGE_TOGGLE_RATING, JSON.stringify(data));
    });
    this.logger.log(`${SocketService.name} messageRated end`);
  }
}
