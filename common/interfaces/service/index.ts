import { Address, SessionUnit, WorkSchedule } from "..";
import { ICategory } from "../category";
import { IUser } from "../user";

export interface ServiceExtraData {
    tables?: number[]; //for cafes
}

export interface IServiceBaseData {
    averageSession: number;
    extra?: ServiceExtraData;
    unit: SessionUnit;
    workSchedule: WorkSchedule[];
    address: Address;
    rating?: number;
}

export interface IServiceBaseDataCreate {
    unit: SessionUnit;
    workSchedule: WorkSchedule[] | string;
    averageSession: number;
    address: Address | string;
    extra?: ServiceExtraData;
}

export interface IService extends IServiceBaseData {
    id: string;
    user: IUser;
    createdAt: number;
    name: string;
    category: ICategory;
    blocked: boolean;
    icon?: string;
    trialEndDate?: number;
    paymentEndDate?: number;
}

export interface IServiceCreate extends IServiceBaseDataCreate {
    name: string;
    categoryId: string;
}

export interface IServiceUpdate extends IServiceCreate {
    id: string;
}

export interface IServiceRating {
    id: string;
    rating: number;
}