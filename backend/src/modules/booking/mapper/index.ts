import { BookingEntity } from '@/entities/BookingEntity';
import { BookingDto } from '../dto';
import { ServiceMapper } from '@/modules/service/mapper';
import { UserMapper } from '@/modules/user/mapper';
import { BranchMapper } from '@/modules/branch/mapper';

export class BookingMapper {
  static map(item: BookingEntity): BookingDto {
    const result: BookingDto = {
      id: item.id,
      createdAt: new Date(item.createdAt).getTime(),
      service: ServiceMapper.map(item.service),
      user: UserMapper.map(item.user),
      bookDate: new Date(item.bookDate).getTime(),
      status: item.status,
    };
    if (item.extra) result.extra = item.extra;
    if (item.notes) result.notes = item.notes;
    if (item.branch) result.branch = BranchMapper.map(item.branch);
    return result;
  }

  static mapList(items: BookingEntity[]) {
    return items.map((v) => BookingMapper.map(v));
  }
}
