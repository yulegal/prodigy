import { Module } from '@nestjs/common';
import { GlobalCronService } from './global-cron.service';
import { GlobalCronEmitterService } from './global-cron-emitter.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { BookingEntity } from '@/entities/BookingEntity';
import { ServiceEntity } from '@/entities/ServiceEntity';
import { NotificationEntity } from '@/entities/NotificationEntity';
import { NotificationModule } from '../notification/notification.module';
import { UserEnitity } from '@/entities/UserEntity';

@Module({
  providers: [GlobalCronService, GlobalCronEmitterService],
  exports: [GlobalCronService, GlobalCronEmitterService],
  imports: [
    TypeOrmModule.forFeature([
      BookingEntity,
      ServiceEntity,
      NotificationEntity,
      UserEnitity,
    ]),
    NotificationModule,
  ],
})
export class GlobalCronModule {}
