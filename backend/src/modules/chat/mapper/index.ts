import { ChatEntity } from '@/entities/ChatEntity';
import { ChatDto } from '../dto';
import { UserMapper } from '@/modules/user/mapper';

export class ChatMapper {
  static map(item: ChatEntity): ChatDto {
    return {
      id: item.id,
      createdAt: new Date(item.createdAt).getTime(),
      user1: UserMapper.map(item.user1),
      user2: UserMapper.map(item.user2),
    };
  }

  static mapList(items: ChatEntity[]) {
    return items.map((v) => ChatMapper.map(v));
  }
}
