import { Column, Entity } from 'typeorm';
import { BaseEntity } from './BaseEntity';

@Entity('roles')
export class RoleEntity extends BaseEntity {
  @Column({ length: 100, nullable: false })
  name: string;
}
