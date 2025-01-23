import { Module } from '@nestjs/common';
import { BookingController } from './booking.controller';
import { BookingService } from './booking.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { BookingEntity } from '@/entities/BookingEntity';
import { UserEnitity } from '@/entities/UserEntity';
import { ServiceEntity } from '@/entities/ServiceEntity';
import { NotificationModule } from '../notification/notification.module';
import { BranchEntity } from '@/entities/BranchEntity';

@Module({
  controllers: [BookingController],
  providers: [BookingService],
  exports: [BookingService],
  imports: [
    TypeOrmModule.forFeature([
      BookingEntity,
      UserEnitity,
      ServiceEntity,
      BranchEntity,
    ]),
    NotificationModule,
  ],
})
export class BookingModule {}
