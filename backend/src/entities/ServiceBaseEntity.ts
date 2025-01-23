import { Column } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { ServiceExtraData } from '@shared/interfaces/service';
import { Address, SessionUnit, WorkSchedule } from '@shared/interfaces';

export abstract class ServiceBaseEntity extends BaseEntity {
  @Column({ type: 'json', nullable: true })
  extra: ServiceExtraData;

  @Column({ type: 'integer' })
  averageSession: number;

  @Column({ type: 'enum', enum: SessionUnit })
  unit: SessionUnit;

  @Column({ type: 'json' })
  address: Address;

  @Column({ type: 'json' })
  workSchedule: WorkSchedule[];
}
