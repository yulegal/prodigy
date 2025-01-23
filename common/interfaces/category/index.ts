import { ILocaleValue } from "..";

export enum CategoryType {
    HAIRCUT = 'HAIRCUT',
    CAFE = 'CAFE',
}

export interface ICategory {
    id: string;
    name: ILocaleValue;
    type: CategoryType;
    createdAt: number;
    icon?: string;
}

export interface ICategoryCreate {
    name: ILocaleValue;
    type: CategoryType;
}

export interface ICateogryUpdate extends ICategoryCreate {
    id: string;
}
