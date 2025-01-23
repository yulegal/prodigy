import { ChatDto } from '@/modules/chat/dto';
import { UserDto } from '@/modules/user/dto';
import {
  IMessage,
  IMessageCreate,
  IMessageForward,
  IMessageUpdate,
} from '@shared/interfaces/message';

export class MessageDto implements IMessage {
  id: string;
  chat: ChatDto;
  from: UserDto;
  to: UserDto;
  isRead: boolean;
  createdAt: number;
  addons?: string[];
  body?: string;
  parent?: MessageDto;
  forwardedFrom?: UserDto;
  edited: boolean;
}

export class MessageCreateDto implements IMessageCreate {
  body?: string;
  parentId?: string;
  toId: string;
}

export class MessageUpdateDto
  extends MessageCreateDto
  implements IMessageUpdate
{
  id: string;
}

export class MessageForwardDto implements IMessageForward {
  id: string;
  toId: string;
}
