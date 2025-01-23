import { Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { UserEnitity } from './UserEntity';
import { BranchEntity } from './BranchEntity';

@Entity('branch_users')
export class BranchUserEntity extends BaseEntity {
  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user: UserEnitity;

  @ManyToOne(() => BranchEntity, { onDelete: 'CASCADE' })
  branch: BranchEntity;
}
