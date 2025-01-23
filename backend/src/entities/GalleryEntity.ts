import { Column, Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { ServiceEntity } from './ServiceEntity';
import { BranchEntity } from './BranchEntity';

@Entity('galleries')
export class GalleryEntity extends BaseEntity {
  @ManyToOne(() => ServiceEntity, { onDelete: 'CASCADE' })
  service: ServiceEntity;

  @ManyToOne(() => BranchEntity, { onDelete: 'CASCADE', nullable: true })
  branch: BranchEntity;

  @Column({ length: 200 })
  photo: string;
}
