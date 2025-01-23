import { Module } from '@nestjs/common';
import { PublicController } from './public.controller';
import { ConfigModule } from '@/core/config/config.module';

@Module({
  controllers: [PublicController],
  imports: [ConfigModule],
})
export class PublicModule {}
