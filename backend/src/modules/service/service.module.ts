import { Module } from '@nestjs/common';
import { ServiceController } from './service.controller';
import { ServiceService } from './service.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ServiceEntity } from '@/entities/ServiceEntity';
import { UserEnitity } from '@/entities/UserEntity';
import { ConfigModule } from '@/core/config/config.module';
import { CategoryEntity } from '@/entities/CategoryEntity';
import { ServiceRatingEntity } from '@/entities/ServiceRatingEntity';
import { FavoriteEntity } from '@/entities/FavoriteEntity';

@Module({
  controllers: [ServiceController],
  providers: [ServiceService],
  exports: [ServiceService],
  imports: [
    TypeOrmModule.forFeature([
      ServiceEntity,
      UserEnitity,
      CategoryEntity,
      ServiceRatingEntity,
      FavoriteEntity,
    ]),
    ConfigModule,
  ],
})
export class ServiceModule {}
