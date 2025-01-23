import { Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { UserEnitity } from './UserEntity';

@Entity('contacts')
export class ContactEntity extends BaseEntity {
  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user: UserEnitity;

  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  contact: UserEnitity;
}
