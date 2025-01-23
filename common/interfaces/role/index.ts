import { IPageableResponseData } from '..';

export interface IRole {
    id: string;
    name: string;
}

export interface IRolePageableResponseData extends IPageableResponseData<IRole> {}