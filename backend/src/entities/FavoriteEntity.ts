import { Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { UserEnitity } from './UserEntity';
import { ServiceEntity } from './ServiceEntity';

@Entity('favorites')
export class FavoriteEntity extends BaseEntity {
  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user: UserEnitity;

  @ManyToOne(() => ServiceEntity, { onDelete: 'CASCADE' })
  service: ServiceEntity;
}
