import { Column, Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { UserEnitity } from './UserEntity';
import { NotificationType } from '@shared/interfaces/notification';

@Entity('notifications')
export class NotificationEntity extends BaseEntity {
  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user: UserEnitity;

  @Column({ length: 100 })
  title: string;

  @Column({ length: 250 })
  body: string;

  @Column({ default: false })
  isRead: boolean;

  @Column({ type: 'enum', enum: NotificationType })
  type: NotificationType;

  @Column({ nullable: true, type: 'uuid' })
  itemId: string;
}
