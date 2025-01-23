import { IService } from "../service";
import { IUser } from "../user";
import { IBranch } from '../branch';

export enum BookingStatus {
    ACTIVE = 'ACTIVE',
    CANCELED = 'CANCELED',
    DONE = 'DONE',
}

export interface BookingExtraData {
    table?: number; //for cafes
}

export interface IBooking {
    id: string;
    user: IUser;
    createdAt: number;
    bookDate: number;
    service: IService;
    extra?: BookingExtraData;
    branch?: IBranch;
    status: BookingStatus;
    notes?: string;
}

export interface IBookingCreate {
    bookDate: number;
    serviceId: string;
    branchId?: string;
    extra?: BookingExtraData;
    notes?: string;
}

export interface IBookingRebook extends IBookingCreate {
    id: string;
}