import { Column, Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { UserEnitity } from './UserEntity';
import { MessageEntity } from './MessageEntity';

@Entity('message_ratings')
export class MessageRatingEntity extends BaseEntity {
  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user: UserEnitity;

  @ManyToOne(() => MessageEntity, { onDelete: 'CASCADE' })
  message: MessageEntity;

  @Column({ length: 40 })
  face: string;
}
