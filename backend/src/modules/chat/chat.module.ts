import { Module } from '@nestjs/common';
import { ChatController } from './chat.controller';
import { ChatService } from './chat.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ChatEntity } from '@/entities/ChatEntity';
import { MessageEntity } from '@/entities/MessageEntity';

@Module({
  controllers: [ChatController],
  providers: [ChatService],
  exports: [ChatService],
  imports: [TypeOrmModule.forFeature([ChatEntity, MessageEntity])],
})
export class ChatModule {}
