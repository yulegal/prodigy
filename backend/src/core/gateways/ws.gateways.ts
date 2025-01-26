import {
  ConnectedSocket,
  MessageBody,
  OnGatewayConnection,
  OnGatewayDisconnect,
  OnGatewayInit,
  SubscribeMessage,
  WebSocketGateway,
  WebSocketServer,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { SocketService } from '../socket/socket.service';
import { Inject, Logger } from '@nestjs/common';
import { EVENTS, REDIS_NAMESPACE, WS_MESSAGES } from '../defs';
import { RedisClientService } from '../redis-client/redis-client.service';
import { EventEmitter2 } from '@nestjs/event-emitter';

@WebSocketGateway({
  cors: true,
  path: '/api/events',
})
export class WsGateway
  implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect
{
  @WebSocketServer() server: Server;
  private readonly logger;
  @Inject(RedisClientService)
  private readonly redisClientService: RedisClientService;

  constructor(
    private readonly socketService: SocketService,
    private readonly emitter: EventEmitter2,
  ) {
    this.logger = new Logger(WsGateway.name);
  }

  @SubscribeMessage(WS_MESSAGES.CLIENT.I_AM)
  async initMessage(
    @MessageBody() payload: string,
    @ConnectedSocket() client: Socket,
  ) {
    this.logger.log(`${WsGateway.name} initMessage start`);
    if (payload && payload != 'null') {
      const user = JSON.parse(payload);
      await this.redisClientService.set(REDIS_NAMESPACE.WS + client.id, user);
      this.emitter.emit(EVENTS.HANDLE_USER_ONLINE, user);
    }
    this.logger.log(`${WsGateway.name} initMessage end`);
  }

  afterInit(server: any) {
    this.socketService.socket = server;
    this.logger.log('Init websocket gateway');
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  handleConnection(client: any) {
    this.logger.log('Socket client connected ' + client.id);
    this.server.to(client.id).emit(WS_MESSAGES.SERVER.WHO_ARE_YOU);
  }

  async handleDisconnect(client: any) {
    this.logger.log(`Client disconnected: ${client.id}`);
    const user = await this.redisClientService.get(
      REDIS_NAMESPACE.WS + client.id,
    );
    this.emitter.emit(EVENTS.HANDLE_USER_OFFLINE, user);
    await this.redisClientService.del(REDIS_NAMESPACE.WS + client.id);
  }

  @SubscribeMessage(WS_MESSAGES.CLIENT.LOGOUT)
  async logout(@ConnectedSocket() client: Socket) {
    this.logger.log(`Client logged out: ${client.id}`);
    const user = await this.redisClientService.get(
      REDIS_NAMESPACE.WS + client.id,
    );
    await this.redisClientService.del(REDIS_NAMESPACE.WS + client.id);
    this.emitter.emit(EVENTS.USER_LOGGED_OUT, client.id, user);
  }

  @SubscribeMessage(WS_MESSAGES.CLIENT.MESSAGE_READ)
  async messageRead(@MessageBody() id: string) {
    this.logger.log(`${WsGateway.name} messageRead start`);
    this.emitter.emit(EVENTS.MESSAGE_READ, id);
    this.logger.log(`${WsGateway.name} messageRead end`);
  }

  @SubscribeMessage(WS_MESSAGES.CLIENT.MESSAGE_RATED)
  async rated(@MessageBody() data: string, @ConnectedSocket() client: Socket) {
    this.logger.log(`${WsGateway.name} rated start`);
    const user = await this.redisClientService.get(
      REDIS_NAMESPACE.WS + client.id,
    );
    this.emitter.emit(EVENTS.MESSAGE_RATED, JSON.parse(data), user);
    this.logger.log(`${WsGateway.name} rated end`);
  }

  @SubscribeMessage(WS_MESSAGES.CLIENT.CHECK_USER_ONLINE)
  async checkUserOnline(
    @MessageBody() id: string,
    @ConnectedSocket() client: Socket,
  ) {
    this.logger.log(`${WsGateway.name} checkUserOnline start`);
    this.emitter.emit(EVENTS.CHECK_USER_ONLINE, id, client.id);
    this.logger.log(`${WsGateway.name} checkUserOnline end`);
  }
}
