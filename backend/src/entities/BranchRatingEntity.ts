import { Column, Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { UserEnitity } from './UserEntity';
import { BranchEntity } from './BranchEntity';

@Entity('branch_ratings')
export class BranchRatingEntity extends BaseEntity {
  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user: UserEnitity;

  @ManyToOne(() => BranchEntity, { onDelete: 'CASCADE' })
  branch: BranchEntity;

  @Column({ type: 'integer' })
  rating: number;
}
