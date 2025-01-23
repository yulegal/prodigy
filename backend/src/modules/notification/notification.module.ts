import { Module } from '@nestjs/common';
import { NotificationController } from './notification.controller';
import { NotificationService } from './notification.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { NotificationEntity } from '@/entities/NotificationEntity';
import { UserEnitity } from '@/entities/UserEntity';
import { BookingEntity } from '@/entities/BookingEntity';

@Module({
  controllers: [NotificationController],
  providers: [NotificationService],
  exports: [NotificationService],
  imports: [
    TypeOrmModule.forFeature([NotificationEntity, UserEnitity, BookingEntity]),
  ],
})
export class NotificationModule {}
