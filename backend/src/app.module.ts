import {
  MiddlewareConsumer,
  Module,
  NestModule,
  RequestMethod,
} from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { TypeOrmModule } from '@nestjs/typeorm';
import { dataSourceOptions } from 'data-source';
import * as path from 'path';
import { AuthMiddleware } from './core/middleware/auth.middleware';
import { ConfigModule as CustomConfigModule } from './core/config/config.module';
import { PublicModule } from './modules/public/public.module';
import { RoleModule } from './modules/role/role.module';
import { ScheduleModule } from '@nestjs/schedule';
import { GlobalCronModule } from './modules/global-cron/global-cron.module';
import { EventEmitterModule } from '@nestjs/event-emitter';
import { WsGatewayModule } from './core/gateways/ws.gateway.module';
import { SocketModule } from './core/socket/socket.module';
import { RedisClientModule } from './core/redis-client/redis-client.module';
import { UserModule } from './modules/user/user.module';
import { AuthOptMiddleware } from './core/middleware/auth-opt.middleware';
import { BookingModule } from './modules/booking/booking.module';
import { ServiceModule } from './modules/service/service.module';
import { CategoryModule } from './modules/category/category.module';
import { BranchModule } from './modules/branch/branch.module';
import { NotificationModule } from './modules/notification/notification.module';
import { GalleryModule } from './modules/gallery/gallery.module';
import { ChatModule } from './modules/chat/chat.module';
import { MessageModule } from './modules/message/message.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      envFilePath: path.join(process.cwd(), '.env'),
    }),
    TypeOrmModule.forRoot(dataSourceOptions),
    UserModule,
    CustomConfigModule,
    PublicModule,
    RoleModule,
    ScheduleModule.forRoot(),
    GlobalCronModule,
    EventEmitterModule.forRoot(),
    WsGatewayModule,
    SocketModule,
    RedisClientModule,
    BookingModule,
    ServiceModule,
    CategoryModule,
    BranchModule,
    NotificationModule,
    GalleryModule,
    ChatModule,
    MessageModule,
  ],
  providers: [JwtService],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(AuthMiddleware)
      .exclude(
        { path: 'public_files/:file', method: RequestMethod.GET },
        { path: 'public_files/addon/:file', method: RequestMethod.GET },
        { path: 'branch/:id', method: RequestMethod.GET },
        { path: 'branch/filter', method: RequestMethod.POST },
        { path: 'service/filter', method: RequestMethod.POST },
        { path: 'user/login', method: RequestMethod.POST },
        { path: 'user', method: RequestMethod.POST },
        { path: 'category/filter', method: RequestMethod.POST },
        { path: 'service/:id', method: RequestMethod.GET },
      )
      .forRoutes({ path: '*', method: RequestMethod.ALL });
    consumer
      .apply(AuthOptMiddleware)
      .forRoutes(
        { path: 'branch/:id', method: RequestMethod.GET },
        { path: 'branch/filter', method: RequestMethod.POST },
        { path: 'service/filter', method: RequestMethod.POST },
        { path: 'service/:id', method: RequestMethod.GET },
      );
  }
}
