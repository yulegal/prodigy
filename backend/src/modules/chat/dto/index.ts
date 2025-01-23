import { UserDto } from '@/modules/user/dto';
import { IChat } from '@shared/interfaces/chat';

export class ChatDto implements IChat {
  id: string;
  user1: UserDto;
  user2: UserDto;
  createdAt: number;
}
