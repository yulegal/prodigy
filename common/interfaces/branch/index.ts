import { IService, IServiceBaseData, IServiceBaseDataCreate } from '../service';
import { IUser } from '../user';

export interface IBranch extends IServiceBaseData {
    id: string;
    createdAt: number;
    service: IService;
    users: IUser[];
}

export interface IBranchCreate extends IServiceBaseDataCreate {
    serviceId: string;
    userIds: string[];
}

export interface IBranchRating {
    id: string;
    rating: number;
}

export interface IBranchUpdate extends IBranchCreate {
    id: string;
}