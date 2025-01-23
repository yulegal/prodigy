import { ILocaleValue } from '@shared/interfaces';
import {
  CategoryType,
  ICategory,
  ICategoryCreate,
  ICateogryUpdate,
} from '@shared/interfaces/category';

export class CategoryDto implements ICategory {
  id: string;
  name: ILocaleValue;
  type: CategoryType;
  createdAt: number;
  icon?: string;
}

export class CategoryCreateDto implements ICategoryCreate {
  name: ILocaleValue;
  type: CategoryType;
}

export class CategoryUpdateDto
  extends CategoryCreateDto
  implements ICateogryUpdate
{
  id: string;
}
