import { Column, Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { UserEnitity } from './UserEntity';
import { ServiceEntity } from './ServiceEntity';

@Entity('service_ratings')
export class ServiceRatingEntity extends BaseEntity {
  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user: UserEnitity;

  @ManyToOne(() => ServiceEntity, { onDelete: 'CASCADE' })
  service: ServiceEntity;

  @Column({ type: 'integer' })
  rating: number;
}
