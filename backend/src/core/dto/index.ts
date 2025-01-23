import {
  ComparisonOperator,
  Direction,
  IFilterItem,
  IFilterQuery,
} from '@shared/interfaces';

export class FilterItem implements IFilterItem {
  name: string;
  value: string | number;
  comparison?: ComparisonOperator;
}

export class FilterQueryOptions implements IFilterQuery {
  page: number;
  limit: number;
  filter?: FilterItem[];
  sortDirection?: Direction;
  sortField?: string;
}

export class PageableResponseData {
  count: number;
  page: number;
  pageCount: number;
  total: number;
}
