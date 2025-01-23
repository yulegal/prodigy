export interface IError {
    code: number | null;
    message: string | null;
    params?: any;
}

export enum ComparisonOperator {
    EQUAL = 'eq',
    NOT_EQUAL = 'neq',
    GREATER_THAN = 'gt',
    LESS_THAN = 'lt',
    GREATER_THAN_OR_EQUAL = 'gte',
    LESS_THAN_OR_EQUAL = 'lte',
    LIKE = 'like',
    ILIKE = 'ilike',
}

export enum Direction {
    DESC = 'desc',
    ASC = 'asc'
}

export interface IFilterItem {
    name: string;
    value: string | number;
    comparison?: ComparisonOperator;
}

export interface IFilterQuery {
    page: number;
    limit: number;
    filter?: IFilterItem[];
    sortField?: string;
    sortDirection?: Direction;
}

export interface IPageable {
    count: number;
    page: number;
    pageCount: number;
    total: number;
}

export interface IPageableResponseData<T> extends IPageable, IResponse<T> {}

export interface IResponse<T> {
    data: T | T[] | null;
}

export interface ILocaleValue {
    [key: string]: string;
}

export enum Locale {
    EN = 'en',
    RU = 'ru'
}

export interface IFile {
	name: string;
	size: number;
	type: string;
	extension: string;
	base64: string;
}

export enum SessionUnit {
    HOURS = 'hours',
    MINUTES = 'minutes',
}

export enum WeekDay {
    MONDAY = 'MONDAY',
    TUESDAY = 'TUESDAY',
    WEDNESDAY = 'WEDNESDAY',
    THURSDAY = 'THURSDAY',
    FRIDAY = 'FRIDAY',
    SATURDAY = 'SATURDAY',
    SUNDAY = 'SUNDAY',
}

export interface WorkSchedule {
    startTime?: number;
    endTime?: number;
    weekDay: WeekDay;
    allDay: boolean;
}

export interface Address {
    address: string;
    url?: string;
}