import { Column, Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { ServiceEntity } from './ServiceEntity';
import { UserEnitity } from './UserEntity';
import { BookingExtraData, BookingStatus } from '@shared/interfaces/booking';
import { BranchEntity } from './BranchEntity';

@Entity('bookings')
export class BookingEntity extends BaseEntity {
  @ManyToOne(() => ServiceEntity, { onDelete: 'CASCADE' })
  service: ServiceEntity;

  @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
  user: UserEnitity;

  @Column()
  bookDate: Date;

  @Column({ type: 'enum', enum: BookingStatus })
  status: BookingStatus;

  @Column({ type: 'json', nullable: true })
  extra: BookingExtraData;

  @ManyToOne(() => BranchEntity, { onDelete: 'CASCADE', nullable: true })
  branch: BranchEntity;

  @Column({ length: 150, nullable: true })
  notes: string;
}
