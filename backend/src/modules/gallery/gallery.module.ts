import { Module } from '@nestjs/common';
import { GalleryController } from './gallery.controller';
import { GalleryService } from './gallery.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { GalleryEntity } from '@/entities/GalleryEntity';
import { ServiceEntity } from '@/entities/ServiceEntity';
import { BranchEntity } from '@/entities/BranchEntity';
import { ConfigModule } from '@/core/config/config.module';

@Module({
  controllers: [GalleryController],
  providers: [GalleryService],
  exports: [GalleryService],
  imports: [
    TypeOrmModule.forFeature([GalleryEntity, ServiceEntity, BranchEntity]),
    ConfigModule,
  ],
})
export class GalleryModule {}
