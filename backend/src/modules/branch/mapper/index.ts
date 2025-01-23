import { BranchEntity } from '@/entities/BranchEntity';
import { BranchDto } from '../dto';
import { ServiceMapper } from '@/modules/service/mapper';
import { UserMapper } from '@/modules/user/mapper';

export class BranchMapper {
  static map(item: BranchEntity): BranchDto {
    const result: BranchDto = {
      id: item.id,
      createdAt: new Date(item.createdAt).getTime(),
      service: ServiceMapper.map(item.service),
      averageSession: item.averageSession,
      unit: item.unit,
      workSchedule: item.workSchedule,
      address: item.address,
      users: item.users.map((v) => UserMapper.map(v.user)),
    };
    if (item.extra) result.extra = item.extra;
    return result;
  }

  static mapList(items: BranchEntity[]) {
    return items.map((v) => BranchMapper.map(v));
  }
}
