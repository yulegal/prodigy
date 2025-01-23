import { CategoryDto } from '@/modules/category/dto';
import { UserDto } from '@/modules/user/dto';
import { SessionUnit, WorkSchedule, Address } from '@shared/interfaces';
import {
  IService,
  IServiceCreate,
  IServiceRating,
  IServiceUpdate,
  ServiceExtraData,
} from '@shared/interfaces/service';

export class ServiceDto implements IService {
  id: string;
  user: UserDto;
  createdAt: number;
  name: string;
  category: CategoryDto;
  blocked: boolean;
  averageSession: number;
  extra?: ServiceExtraData;
  unit: SessionUnit;
  workSchedule: WorkSchedule[];
  address: Address;
  rating?: number;
  icon?: string;
  paymentEndDate?: number;
  trialEndDate?: number;
}

export class ServiceCreateDto implements IServiceCreate {
  name: string;
  categoryId: string;
  unit: SessionUnit;
  workSchedule: string | WorkSchedule[];
  averageSession: number;
  address: string | Address;
  extra?: ServiceExtraData;
}

export class ServiceUpdateDto
  extends ServiceCreateDto
  implements IServiceUpdate
{
  id: string;
}

export class ServiceRatingDto implements IServiceRating {
  id: string;
  rating: number;
}
