import { BaseData } from '..';
import { IChat } from '../chat';
import { IUser } from '../user';

export interface IMessage {
    id: string;
    chat: IChat;
    from: IUser;
    to: IUser;
    isRead: boolean;
    createdAt: number;
    addons?: string[];
    body?: string;
    parent?: IMessage;
    forwardedFrom?: IUser;
    edited: boolean;
    rating?: BaseData[];
}

export interface IMessageCreate {
    body?: string;
    parentId?: string;
    toId: string;
}

export interface IMessageUpdate extends IMessageCreate {
    id: string;
}

export interface IMessageForward {
    id: string;
    toId: string;
}