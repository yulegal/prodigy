import { ServiceEntity } from '@/entities/ServiceEntity';
import { ServiceDto } from '../dto';
import { UserMapper } from '@/modules/user/mapper';
import { CategoryMapper } from '@/modules/category/mapper';

export class ServiceMapper {
  static map(item: ServiceEntity): ServiceDto {
    const result: ServiceDto = {
      id: item.id,
      createdAt: new Date(item.createdAt).getTime(),
      name: item.name,
      user: UserMapper.map(item.user),
      averageSession: item.averageSession,
      unit: item.unit,
      blocked: item.blocked,
      category: CategoryMapper.map(item.category),
      workSchedule: item.workSchedule,
      address: item.address,
    };
    if (item.trialEndDate)
      result.trialEndDate = new Date(item.trialEndDate).getTime();
    if (item.paymentEndDate)
      result.paymentEndDate = new Date(item.paymentEndDate).getTime();
    if (item.icon) result.icon = item.icon;
    if (item.extra) result.extra = item.extra;
    return result;
  }

  static mapList(items: ServiceEntity[]) {
    return items.map((v) => ServiceMapper.map(v));
  }
}
