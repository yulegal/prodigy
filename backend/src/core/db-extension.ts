import { ComparisonOperator, Direction } from '@shared/interfaces';
import {
  Equal,
  FindOptionsWhere,
  ILike,
  LessThan,
  LessThanOrEqual,
  Like,
  MoreThan,
  MoreThanOrEqual,
  Not,
  Repository,
} from 'typeorm';
import { FilterQueryOptions } from './dto';

export const getPageableData = async (
  repository: Repository<any>,
  options: FilterQueryOptions,
  //eslint-disable-next-line @typescript-eslint/ban-types
  map: Function,
  relations: string[] = [],
  additionalWhere: FindOptionsWhere<any> | FindOptionsWhere<any>[] = null,
) => {
  let where: FindOptionsWhere<any | any[]> = {};
  const order: any = {
    createdAt: 'DESC',
  };
  if (options.sortField)
    order[options.sortField] = options.sortDirection ?? Direction.DESC;
  if (!Array.isArray(additionalWhere)) {
    if (options.filter?.length) {
      for (const option of options.filter) {
        where = {
          ...where,
          ...getNestedEntity(option.name, option.value, option.comparison),
        };
      }
    }
    if (additionalWhere && !Array.isArray(additionalWhere)) {
      mergeObject(additionalWhere, where);
    }
  } else {
    where = additionalWhere;
  }
  const limit = options.limit ?? 1000;
  const page = options.page ?? 1;
  const [items, count] = await repository.findAndCount({
    where,
    skip: limit * (page - 1),
    take: limit,
    order,
    relations,
  });

  return {
    count,
    total: items.length,
    pageCount: Math.ceil(count / options.limit),
    page: options.page,
    data: await map(items),
  };
};

const mergeObject = (master: any, slave: any) => {
  for (const key of Object.getOwnPropertyNames(master)) {
    if (Object.keys(master[key]).length === 0 || !slave.hasOwnProperty(key)) {
      slave[key] = master[key];
      continue;
    }
    mergeObject(master[key], slave[key]);
  }
};

const getNestedEntity = (
  key: string,
  value: any,
  comparison?: ComparisonOperator,
) => {
  const where: FindOptionsWhere<any> = {};
  const keys = key.split('.');
  for (const k of keys) {
    if (keys.indexOf(k) != keys.length - 1) where[k] = {};
    else where[k] = getOperator(value, comparison);
  }
  return where;
};

const getOperator = (value: any, comparison?: ComparisonOperator) => {
  switch (comparison) {
    case ComparisonOperator.GREATER_THAN:
      return MoreThan(value);
    case ComparisonOperator.GREATER_THAN_OR_EQUAL:
      return MoreThanOrEqual(value);
    case ComparisonOperator.LESS_THAN:
      return LessThan(value);
    case ComparisonOperator.LESS_THAN_OR_EQUAL:
      return LessThanOrEqual(value);
    case ComparisonOperator.NOT_EQUAL:
      return Not(Equal(value));
    case ComparisonOperator.LIKE:
      return Like('%' + value + '%');
    case ComparisonOperator.ILIKE:
      return ILike('%' + value + '%');
  }
  return Equal(value);
};
