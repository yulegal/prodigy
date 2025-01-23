import { IUser } from '../user';

export interface IChat {
    id: string;
    user1: IUser;
    user2: IUser;
    createdAt: number;
}
