import { CategoryEntity } from '@/entities/CategoryEntity';
import { CategoryDto } from '../dto';

export class CategoryMapper {
  static map(item: CategoryEntity): CategoryDto {
    if (!item) return null;
    const result: CategoryDto = {
      id: item.id,
      name: item.name,
      createdAt: new Date(item.createdAt).getTime(),
      type: item.type,
    };
    if (item.icon) result.icon = item.icon;
    return result;
  }

  static mapList(items: CategoryEntity[]) {
    return items.map((v) => CategoryMapper.map(v));
  }
}
