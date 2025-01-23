import { Entity, ManyToOne, OneToMany } from 'typeorm';
import { ServiceEntity } from './ServiceEntity';
import { ServiceBaseEntity } from './ServiceBaseEntity';
import { BranchUserEntity } from './BranchUserEntity';
import { BranchRatingEntity } from './BranchRatingEntity';

@Entity('branches')
export class BranchEntity extends ServiceBaseEntity {
  @ManyToOne(() => ServiceEntity, { onDelete: 'CASCADE' })
  service: ServiceEntity;

  @OneToMany(() => BranchUserEntity, (item) => item.branch, { cascade: true })
  users: BranchUserEntity[];

  @OneToMany(() => BranchRatingEntity, (item) => item.branch, { cascade: true })
  ratings: BranchRatingEntity[];
}
