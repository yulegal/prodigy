import { Column, Entity } from 'typeorm';
import { BaseEntity } from './BaseEntity';
import { ILocaleValue } from '@shared/interfaces';
import { CategoryType } from '@shared/interfaces/category';

@Entity('categories')
export class CategoryEntity extends BaseEntity {
  @Column({ type: 'json' })
  name: ILocaleValue;

  @Column({ nullable: true })
  icon: string;

  @Column({ type: 'enum', enum: CategoryType })
  type: CategoryType;
}
