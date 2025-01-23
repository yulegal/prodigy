import { Column, Entity, ManyToOne, OneToMany } from 'typeorm';
import { UserEnitity } from './UserEntity';
import { CategoryEntity } from './CategoryEntity';
import { ServiceBaseEntity } from './ServiceBaseEntity';
import { ServiceRatingEntity } from './ServiceRatingEntity';

@Entity('services')
export class ServiceEntity extends ServiceBaseEntity {
  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user: UserEnitity;

  @Column({ length: 150 })
  name: string;

  @ManyToOne(() => CategoryEntity, { onDelete: 'CASCADE' })
  category: CategoryEntity;

  @Column({ default: false })
  blocked: boolean;

  @Column({ nullable: true })
  paymentEndDate: Date;

  @Column({ nullable: true })
  trialEndDate: Date;

  @Column({ type: 'integer', nullable: true })
  feePerMonth: number;

  @Column({ length: 200, nullable: true })
  icon: string;

  @OneToMany(() => ServiceRatingEntity, (item) => item.service, {
    cascade: true,
  })
  ratings: ServiceRatingEntity;
}
