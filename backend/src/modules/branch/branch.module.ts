import { Module } from '@nestjs/common';
import { BranchController } from './branch.controller';
import { BranchService } from './branch.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { BranchEntity } from '@/entities/BranchEntity';
import { ServiceEntity } from '@/entities/ServiceEntity';
import { UserEnitity } from '@/entities/UserEntity';
import { NotificationModule } from '../notification/notification.module';
import { BranchRatingEntity } from '@/entities/BranchRatingEntity';

@Module({
  controllers: [BranchController],
  providers: [BranchService],
  exports: [BranchService],
  imports: [
    TypeOrmModule.forFeature([
      BranchEntity,
      ServiceEntity,
      UserEnitity,
      BranchRatingEntity,
    ]),
    NotificationModule,
  ],
})
export class BranchModule {}
