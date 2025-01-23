import { IUser } from "../user";

export enum NotificationType {
    NEW_BOOKING = 'NEW_BOOKING',
    BOOKING_CANCELED = 'BOOKING_CANCELED',
    USER_ADDED_TO_BRANCH = 'USER_ADDED_TO_BRANCH',
    USER_REMOVED_FROM_BRANCH = 'USER_REMOVED_FROM_BRANCH',
    REBOOKED = 'REBOOKED',
    FEE_CHARGED = 'FEE_CHARGED',
    PAYMENT_PERIOD_APPROACHES = 'PAYMENT_PERIOD_APPROACHES',
    SERVICE_BLOCKED_DUE_TO_LACK_BALANCE = 'SERVICE_BLOCKED_DUE_TO_LACK_BALANCE',
    TRIAL_PERIOD_END_APPROACHES = 'TRIAL_PERIOD_END_APPROACHES',
    BOOKING_FINISHED = 'BOOKING_FINISHED',
    NEW_BROADCAST = 'NEW_BROADCAST',
}

export enum BROADCAST_ACTION {
    CANCEL_BOOKING = 'CANCEL_BOOKING'
}

export interface INotification {
    id: string;
    title: string;
    body: string;
    createdAt: number;
    user: IUser;
    type: NotificationType;
    isRead: boolean;
    itemId?: string;
}

export interface IBroadcastCreate {
    message: string;
    date: number;
    action: BROADCAST_ACTION;
    branchId?: string;
}