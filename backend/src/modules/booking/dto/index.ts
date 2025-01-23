import { BranchDto } from '@/modules/branch/dto';
import { ServiceDto } from '@/modules/service/dto';
import { UserDto } from '@/modules/user/dto';
import {
  BookingExtraData,
  BookingStatus,
  IBooking,
  IBookingCreate,
  IBookingRebook,
} from '@shared/interfaces/booking';

export class BookingDto implements IBooking {
  id: string;
  user: UserDto;
  createdAt: number;
  bookDate: number;
  service: ServiceDto;
  extra?: BookingExtraData;
  branch?: BranchDto;
  status: BookingStatus;
  notes?: string;
}

export class BookingCreateDto implements IBookingCreate {
  bookDate: number;
  serviceId: string;
  branchId?: string;
  extra?: BookingExtraData;
  notes?: string;
}

export class BookingRebookDto
  extends BookingCreateDto
  implements IBookingRebook
{
  id: string;
}
