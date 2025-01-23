import { Column, Entity, ManyToOne } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { RoleEntity } from './RoleEntity';
import { Locale } from '@shared/interfaces';
import { UserSettings } from '@shared/interfaces/user';

@Entity('users')
export class UserEnitity extends BaseEntity {
  @Column({ length: 60, nullable: false })
  name: string;

  @Column({ length: 100, nullable: false, unique: true })
  login: string;

  @Column({ nullable: true })
  verifiedAt: Date;

  @Column({ length: 300, nullable: true })
  hash: string;

  @Column({ length: 300, nullable: true })
  key: string;

  @ManyToOne(() => RoleEntity, { cascade: true, nullable: false })
  role: RoleEntity;

  @Column({ nullable: false, type: 'enum', enum: Locale })
  locale: Locale;

  @Column({ nullable: true })
  icon: string;

  @Column({ type: 'integer', nullable: true })
  balance: number;

  @Column({ type: 'json', nullable: true })
  settings: UserSettings;

  @Column({ nullable: true, length: 150 })
  status: string;
}
