import { MessageEntity } from '@/entities/MessageEntity';
import { MessageDto } from '../dto';
import { UserMapper } from '@/modules/user/mapper';
import { ChatMapper } from '@/modules/chat/mapper';

export class MessageMapper {
  static map(item: MessageEntity, includeParent = true): MessageDto {
    const result: MessageDto = {
      id: item.id,
      from: UserMapper.map(item.from),
      to: UserMapper.map(item.to),
      isRead: item.isRead,
      createdAt: new Date(item.createdAt).getTime(),
      chat: ChatMapper.map(item.chat),
      edited: item.edited,
    };
    if (item.ratings?.length)
      result.rating = item.ratings.map((v) => ({
        id: v.user.id,
        name: v.face,
      }));
    if (item.body) result.body = item.body;
    if (item.parent && includeParent)
      result.parent = MessageMapper.map(item.parent);
    if (item.addons?.length) result.addons = item.addons;
    if (item.forwardedFrom)
      result.forwardedFrom = UserMapper.map(item.forwardedFrom);
    return result;
  }

  static mapList(items: MessageEntity[]) {
    return items.map((v) => MessageMapper.map(v));
  }
}
