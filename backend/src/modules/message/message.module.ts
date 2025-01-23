import { Module } from '@nestjs/common';
import { MessageController } from './message.controller';
import { MessageService } from './message.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { MessageEntity } from '@/entities/MessageEntity';
import { UserEnitity } from '@/entities/UserEntity';
import { ChatEntity } from '@/entities/ChatEntity';
import { ConfigModule } from '@/core/config/config.module';

@Module({
  controllers: [MessageController],
  providers: [MessageService],
  exports: [MessageService],
  imports: [
    TypeOrmModule.forFeature([MessageEntity, UserEnitity, ChatEntity]),
    ConfigModule,
  ],
})
export class MessageModule {}
