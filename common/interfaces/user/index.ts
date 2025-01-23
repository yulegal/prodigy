import { IPageableResponseData, Locale } from '..';
import { IRole } from '../role';

export interface IUser {
    id: string;
    name: string;
    login: string;
    createdAt: number;
    updatedAt: number;
    isVerified: boolean;
    locale: Locale;
    role: IRole;
    icon?: string;
    settings?: UserSettings;
    balance?: number;
    status?: string;
}

export interface UserSettings {
    //provider settings
    changeStatusToFinishedAutomatically: boolean;
    //user settings
    notifyOfPrematurelyFinishedBookings: boolean;
}

export interface IUserLogin {
    login: string;
}

export interface IUserCreate extends IUserLogin {
    name: string;
    locale: Locale;
}

export interface IUserEditName {
    name: string;
}

export interface IUserContact {
    name: string;
    number: string;
  }

export interface IUserHandleContact {
    data: IUserContact[];
}

export interface IUserPageableResponseData extends IPageableResponseData<IUser> {}