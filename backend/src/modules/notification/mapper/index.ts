import { NotificationEntity } from '@/entities/NotificationEntity';
import { NotificationDto } from '../dto';
import { UserMapper } from '@/modules/user/mapper';

export class NotificationMapper {
  static map(item: NotificationEntity): NotificationDto {
    const result: NotificationDto = {
      id: item.id,
      createdAt: new Date(item.createdAt).getTime(),
      title: item.title,
      body: item.body,
      isRead: item.isRead,
      type: item.type,
      user: UserMapper.map(item.user),
    };
    if (item.itemId) result.itemId = item.itemId;
    return result;
  }

  static mapList(items: NotificationEntity[]) {
    return items.map((v) => NotificationMapper.map(v));
  }
}
