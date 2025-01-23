import { UserEnitity } from '@/entities/UserEntity';
import { UserDto } from '../dto';
import { RoleMapper } from '@/modules/role/mapper';

export class UserMapper {
  static map(item: UserEnitity): UserDto {
    if (!item) return null;

    const result: UserDto = {
      id: item.id,
      name: item.name,
      login: item.login,
      createdAt: new Date(item.createdAt).getTime(),
      isVerified: !!item.verifiedAt,
      updatedAt: new Date(item.updatedAt).getTime(),
      locale: item.locale,
      role: RoleMapper.map(item.role),
    };
    if (item.settings) result.settings = item.settings;
    if (item.balance) result.balance = item.balance;
    if (item.icon) result.icon = item.icon;
    if (item.status) result.status = item.status;
    return result;
  }

  static mapList(items: UserEnitity[]) {
    return items.map((item) => UserMapper.map(item));
  }
}
