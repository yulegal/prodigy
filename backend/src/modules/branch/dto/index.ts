import { SessionUnit, WorkSchedule, Address } from '@shared/interfaces';
import {
  IBranch,
  IBranchCreate,
  IBranchRating,
  IBranchUpdate,
} from '@shared/interfaces/branch';
import { IService, ServiceExtraData } from '@shared/interfaces/service';
import { IUser } from '@shared/interfaces/user';

export class BranchDto implements IBranch {
  id: string;
  createdAt: number;
  service: IService;
  users: IUser[];
  averageSession: number;
  extra?: ServiceExtraData;
  unit: SessionUnit;
  workSchedule: WorkSchedule[];
  address: Address;
  rating?: number;
}

export class BranchRatingDto implements IBranchRating {
  id: string;
  rating: number;
}

export class BranchCreateDto implements IBranchCreate {
  serviceId: string;
  userIds: string[];
  unit: SessionUnit;
  workSchedule: WorkSchedule[];
  averageSession: number;
  address: Address;
  extra?: ServiceExtraData;
}

export class BranchUpdateDto extends BranchCreateDto implements IBranchUpdate {
  id: string;
}
