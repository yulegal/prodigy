import { Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { UserEnitity } from './UserEntity';

@Entity('chats')
export class ChatEntity extends BaseEntity {
  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user1: UserEnitity;

  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user2: UserEnitity;
}
