import { UserDto } from '@/modules/user/dto';
import {
  BROADCAST_ACTION,
  IBroadcastCreate,
  INotification,
  NotificationType,
} from '@shared/interfaces/notification';

export class NotificationDto implements INotification {
  id: string;
  title: string;
  body: string;
  createdAt: number;
  user: UserDto;
  type: NotificationType;
  isRead: boolean;
  itemId?: string;
}

export class NotificationCreateDto {
  title: string;
  body: string;
  type: NotificationType;
  userId: string;
  itemId?: string;
}

export class BroadcastCreateDto implements IBroadcastCreate {
  message: string;
  date: number;
  action: BROADCAST_ACTION;
  branchId?: string;
}
