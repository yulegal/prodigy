import { Column, Entity, ManyToOne, OneToMany } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { UserEnitity } from './UserEntity';
import { ChatEntity } from './ChatEntity';
import { MessageRatingEntity } from './MessageRatingEntity';

@Entity('messages')
export class MessageEntity extends BaseEntity {
  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  from: UserEnitity;

  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  to: UserEnitity;

  @ManyToOne(() => ChatEntity, { onDelete: 'CASCADE' })
  chat: ChatEntity;

  @ManyToOne(() => MessageEntity, { onDelete: 'CASCADE', nullable: true })
  parent: MessageEntity;

  @Column({ length: 1000, nullable: true })
  body: string;

  @Column({ type: 'json', nullable: true })
  addons: string[];

  @Column({ default: false })
  isRead: boolean;

  @ManyToOne(() => UserEnitity, { onDelete: 'SET NULL', nullable: true })
  forwardedFrom: UserEnitity;

  @OneToMany(() => MessageRatingEntity, (item) => item.message, {
    cascade: true,
  })
  ratings: MessageRatingEntity[];

  @Column({ default: false })
  edited: boolean;
}
