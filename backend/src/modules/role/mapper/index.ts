import { RoleEntity } from '@/entities/RoleEntity';
import { RoleDto } from '../dto';

export class RoleMapper {
  static map(item: RoleEntity): RoleDto {
    return {
      id: item.id,
      name: item.name,
    };
  }

  static mapList(items: RoleEntity[]) {
    return items.map((v) => RoleMapper.map(v));
  }
}
