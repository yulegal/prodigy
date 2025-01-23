import { BookingEntity } from '@/entities/BookingEntity';
import { ServiceEntity } from '@/entities/ServiceEntity';
import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  Logger,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import {
  Between,
  FindOptionsWhere,
  IsNull,
  JsonContains,
  LessThanOrEqual,
  MoreThanOrEqual,
  Repository,
} from 'typeorm';
import { BookingCreateDto, BookingRebookDto } from './dto';
import { UserDto } from '../user/dto';
import {
  BOOKING_RELATIONS,
  BRANCH_RELATIONS,
  SERVICE_RELATIONS,
  USER_RELATIONS,
  USER_ROLES,
} from '@/core/defs';
import { BranchEntity } from '@/entities/BranchEntity';
import { BookingExtraData, BookingStatus } from '@shared/interfaces/booking';
import { UserEnitity } from '@/entities/UserEntity';
import { BookingMapper } from './mapper';
import moment from 'moment';
import { getWeekDay } from '@/core/lib/utils';
import {
  error_booking_date_busy,
  error_booking_not_found,
  error_invalid_book_time,
} from '@shared/errors';
import { SessionUnit } from '@shared/interfaces';
import { CategoryType } from '@shared/interfaces/category';
import { NotificationService } from '../notification/notification.service';
import {
  BOOKING_CREATED_NOTIFICATIONS,
  CANCEL_BOOKING_NOTIFICATIONS,
  FINISH_BOOKING_NOTIFICATIONS,
  REBOOKING_NOTIFICATIONS,
} from '@/core/translations';
import { NotificationType } from '@shared/interfaces/notification';
import { FilterQueryOptions } from '@/core/dto';
import { getPageableData } from '@/core/db-extension';

@Injectable()
export class BookingService {
  private logger: Logger;

  constructor(
    @InjectRepository(BookingEntity)
    private readonly repo: Repository<BookingEntity>,
    @InjectRepository(ServiceEntity)
    private readonly serviceRepo: Repository<ServiceEntity>,
    @InjectRepository(BranchEntity)
    private readonly branchRepo: Repository<BranchEntity>,
    @InjectRepository(UserEnitity)
    private readonly userRepo: Repository<UserEnitity>,
    private readonly notificationService: NotificationService,
  ) {
    this.logger = new Logger(BookingService.name);
  }

  async create(dto: BookingCreateDto, currentUser: UserDto) {
    this.logger.log(`${BookingService.name} create start`);
    const service = await this.serviceRepo.findOne({
      where: { id: dto.serviceId },
      relations: SERVICE_RELATIONS,
    });
    const bookDate = new Date(dto.bookDate);
    let branch: BranchEntity | null = null;
    const wd = getWeekDay(bookDate.getDay());
    const ms =
      bookDate.getHours() * 3600 * 1000 + bookDate.getMinutes() * 60 * 1000;
    if (dto.branchId) {
      branch = await this.branchRepo.findOne({
        where: { id: dto.branchId },
        relations: BRANCH_RELATIONS,
      });
      const isValidDate = branch.workSchedule.some(
        (v) =>
          v.weekDay == wd &&
          (v.allDay || !(Number(v.startTime) > ms || Number(v.endTime) < ms)),
      );
      if (!isValidDate) throw new BadRequestException(error_invalid_book_time);
    } else {
      const isValidDate = service.workSchedule.some(
        (v) =>
          v.weekDay == wd &&
          (v.allDay || !(Number(v.startTime) > ms || Number(v.endTime) < ms)),
      );
      if (!isValidDate) throw new BadRequestException(error_invalid_book_time);
    }
    const isBusy = await this.isBusy({
      bookDate: dto.bookDate,
      service,
      branchId: dto.branchId,
      extra: dto.extra,
      averageDuration: branch
        ? branch.averageSession *
          (branch.unit == SessionUnit.HOURS ? 3600 : 60) *
          1000
        : service.averageSession *
          (service.unit == SessionUnit.HOURS ? 3600 : 60) *
          1000,
    });
    if (isBusy) throw new BadRequestException(error_booking_date_busy);
    const booking = new BookingEntity();
    booking.service = service;
    booking.branch = branch;
    booking.bookDate = bookDate;
    booking.status = BookingStatus.ACTIVE;
    if (dto.extra) booking.extra = dto.extra;
    if (dto.notes) booking.notes = dto.notes;
    booking.user = await this.userRepo.findOne({
      where: { id: currentUser.id },
      relations: USER_RELATIONS,
    });
    const result = await this.repo.save(booking);
    if (branch) {
      for (const user of branch.users) {
        await this.notificationService.create({
          title: BOOKING_CREATED_NOTIFICATIONS.title[user.user.locale].replace(
            '%name',
            currentUser.name,
          ),
          body: BOOKING_CREATED_NOTIFICATIONS.body[user.user.locale]
            .replace('%name', currentUser.name)
            .replace('%date', moment(bookDate).format('DD.MM.YYYY HH:mm')),
          type: NotificationType.NEW_BOOKING,
          userId: user.user.id,
          itemId: currentUser.id,
        });
      }
    } else {
      await this.notificationService.create({
        title: BOOKING_CREATED_NOTIFICATIONS.title[service.user.locale].replace(
          '%name',
          currentUser.name,
        ),
        body: BOOKING_CREATED_NOTIFICATIONS.body[service.user.locale]
          .replace('%name', currentUser.name)
          .replace('%date', moment(bookDate).format('DD.MM.YYYY HH:mm')),
        type: NotificationType.NEW_BOOKING,
        userId: service.user.id,
        itemId: currentUser.id,
      });
    }
    this.logger.log(`${BookingService.name} create end`);
    return BookingMapper.map(result);
  }

  async cancelBooking(id: string, currentUser: UserDto) {
    this.logger.log(`${BookingService.name} cancelBooking start`);
    const booking = await this.repo.findOne({
      where: { id },
      relations: BOOKING_RELATIONS,
    });
    if (!booking) throw new BadRequestException(error_booking_not_found);
    if (booking.user.id != currentUser.id) throw new ForbiddenException();
    booking.status = BookingStatus.CANCELED;
    const result = await this.repo.save(booking);
    if (booking.branch) {
      for (const user of booking.branch.users) {
        await this.notificationService.create({
          title: CANCEL_BOOKING_NOTIFICATIONS.title[user.user.locale],
          body: CANCEL_BOOKING_NOTIFICATIONS.body[user.user.locale]
            .replace('%name', currentUser.name)
            .replace(
              '%date',
              moment(booking.bookDate).format('DD.MM.YYYY HH:mm'),
            ),
          type: NotificationType.BOOKING_CANCELED,
          userId: user.user.id,
          itemId: currentUser.id,
        });
      }
    } else {
      await this.notificationService.create({
        title: CANCEL_BOOKING_NOTIFICATIONS.title[booking.service.user.locale],
        body: CANCEL_BOOKING_NOTIFICATIONS.body[booking.service.user.locale]
          .replace('%name', currentUser.name)
          .replace(
            '%date',
            moment(booking.bookDate).format('DD.MM.YYYY HH:mm'),
          ),
        type: NotificationType.BOOKING_CANCELED,
        userId: booking.service.user.id,
        itemId: currentUser.id,
      });
    }
    this.logger.log(`${BookingService.name} cancelBooking end`);
    return BookingMapper.map(result);
  }

  async finishBooking(id: string, currentUser: UserDto) {
    this.logger.log(`${BookingService.name} finishBooking start`);
    const booking = await this.repo.findOne({
      where: { id },
      relations: BOOKING_RELATIONS,
    });
    if (!booking) throw new BadRequestException(error_booking_not_found);
    if (booking.user.id != currentUser.id) throw new ForbiddenException();
    booking.status = BookingStatus.DONE;
    const result = await this.repo.save(booking);
    this.logger.log(`${BookingService.name} finishBooking end`);
    if (booking.branch) {
      for (const user of booking.branch.users) {
        await this.notificationService.create({
          title: FINISH_BOOKING_NOTIFICATIONS.title[user.user.locale],
          body: FINISH_BOOKING_NOTIFICATIONS.body[user.user.locale]
            .replace('%name', currentUser.name)
            .replace(
              '%date',
              moment(booking.bookDate).format('DD.MM.YYYY HH:mm'),
            ),
          type: NotificationType.BOOKING_FINISHED,
          userId: user.user.id,
          itemId: currentUser.id,
        });
      }
    } else {
      await this.notificationService.create({
        title: FINISH_BOOKING_NOTIFICATIONS.title[booking.service.user.locale],
        body: FINISH_BOOKING_NOTIFICATIONS.body[booking.service.user.locale]
          .replace('%name', currentUser.name)
          .replace(
            '%date',
            moment(booking.bookDate).format('DD.MM.YYYY HH:mm'),
          ),
        type: NotificationType.BOOKING_FINISHED,
        userId: booking.service.user.id,
        itemId: currentUser.id,
      });
    }
    return BookingMapper.map(result);
  }

  async rebook(dto: BookingRebookDto, currentUser: UserDto) {
    this.logger.log(`${BookingService.name} rebook start`);
    const booking = await this.repo.findOne({
      where: { id: dto.id },
      relations: BOOKING_RELATIONS,
    });
    if (!booking) throw new BadRequestException(error_booking_not_found);
    if (booking.user.id != currentUser.id) throw new ForbiddenException();
    const bookDate = new Date(dto.bookDate);
    const wd = getWeekDay(bookDate.getDay());
    const ms =
      bookDate.getHours() * 3600 * 1000 + bookDate.getMinutes() * 60 * 1000;
    let branch: BranchEntity | null = null;
    if (dto.branchId) {
      branch = await this.branchRepo.findOne({
        where: { id: dto.branchId },
        relations: BRANCH_RELATIONS,
      });
      const isValidDate = branch.workSchedule.some(
        (v) =>
          v.weekDay == wd &&
          (v.allDay || !(Number(v.startTime) > ms || Number(v.endTime) < ms)),
      );
      if (!isValidDate) throw new BadRequestException(error_invalid_book_time);
    } else {
      const isValidDate = booking.service.workSchedule.some(
        (v) =>
          v.weekDay == wd &&
          (v.allDay || !(Number(v.startTime) > ms || Number(v.endTime) < ms)),
      );
      if (!isValidDate) throw new BadRequestException(error_invalid_book_time);
    }
    const isBusy = await this.isBusy({
      bookDate: dto.bookDate,
      service: booking.service,
      branchId: dto.branchId,
      extra: dto.extra,
      averageDuration: booking.branch
        ? booking.branch.averageSession *
          (booking.branch.unit == SessionUnit.HOURS ? 3600 : 60) *
          1000
        : booking.service.averageSession *
          (booking.service.unit == SessionUnit.HOURS ? 3600 : 60) *
          1000,
    });
    if (isBusy) throw new BadRequestException(error_booking_date_busy);
    booking.bookDate = bookDate;
    booking.branch = branch;
    booking.status = BookingStatus.ACTIVE;
    if (dto.notes) booking.notes = dto.notes;
    if (dto.extra) booking.extra = dto.extra;
    const result = await this.repo.save(booking);
    if (branch) {
      for (const user of branch.users) {
        await this.notificationService.create({
          title: REBOOKING_NOTIFICATIONS.title[user.user.locale].replace(
            '%name',
            currentUser.name,
          ),
          body: REBOOKING_NOTIFICATIONS.body[user.user.locale]
            .replace('%name', currentUser.name)
            .replace('%date', moment(bookDate).format('DD.MM.YYYY HH:mm')),
          type: NotificationType.REBOOKED,
          userId: user.user.id,
          itemId: currentUser.id,
        });
      }
    } else {
      await this.notificationService.create({
        title: REBOOKING_NOTIFICATIONS.title[
          booking.service.user.locale
        ].replace('%name', currentUser.name),
        body: REBOOKING_NOTIFICATIONS.body[booking.service.user.locale]
          .replace('%name', currentUser.name)
          .replace('%date', moment(bookDate).format('DD.MM.YYYY HH:mm')),
        type: NotificationType.REBOOKED,
        userId: booking.service.user.id,
        itemId: currentUser.id,
      });
    }
    this.logger.log(`${BookingService.name} rebook end`);
    return BookingMapper.map(result);
  }

  async filter(options: FilterQueryOptions, currentUser: UserDto) {
    this.logger.log(`${BookingService.name} filter start`);
    const where: FindOptionsWhere<BookingEntity> = {};
    if (options.filter?.length) {
      if (
        currentUser.role.id == USER_ROLES.provider ||
        currentUser.role.id == USER_ROLES.helper
      ) {
        const type = options.filter.find((v) => v.name == 'type')?.value;
        if (type) {
          if (type == 'my') {
            where.user = { id: currentUser.id };
          } else if (type == 'clients') {
            if (currentUser.role.id == USER_ROLES.provider) {
              where.service = {
                user: { id: currentUser.id },
              };
            } else {
              where.branch = {
                users: {
                  user: { id: currentUser.id },
                },
              };
            }
          }
          options.filter = options.filter.filter((v) => v.name != 'type');
        }
      }
      const dateFrom = options.filter.find((v) => v.name == 'dateFrom')?.value;
      const dateTo = options.filter.find((v) => v.name == 'dateTo')?.value;
      if (dateFrom || dateTo) {
        if (dateFrom && dateTo) {
          where.bookDate = Between(
            moment(Number(dateFrom)).startOf('day').toDate(),
            moment(Number(dateTo)).endOf('day').toDate(),
          );
        } else if (dateFrom) {
          where.bookDate = MoreThanOrEqual(
            moment(Number(dateFrom)).startOf('day').toDate(),
          );
        } else {
          where.bookDate = LessThanOrEqual(
            moment(Number(dateTo)).endOf('day').toDate(),
          );
        }
        options.filter = options.filter.filter((v) => v.name != 'dateFrom');
        options.filter = options.filter.filter((v) => v.name != 'dateTo');
      }
    }
    this.logger.log(`${BookingService.name} filter end`);
    return getPageableData(
      this.repo,
      options,
      BookingMapper.mapList,
      BOOKING_RELATIONS,
      where,
    );
  }

  private async isBusy(data: {
    bookDate: number;
    service: ServiceEntity;
    branchId?: string;
    extra?: BookingExtraData;
    averageDuration: number;
  }) {
    this.logger.log(`${BookingService.name} isBusy start`);
    const where: FindOptionsWhere<BookingEntity> = {
      bookDate: Between(
        moment(data.bookDate)
          .subtract(data.averageDuration, 'milliseconds')
          .startOf('minutes')
          .toDate(),
        moment(data.bookDate)
          .add(data.averageDuration, 'milliseconds')
          .endOf('minutes')
          .toDate(),
      ),
      branch: data.branchId ? { id: data.branchId } : IsNull(),
      service: { id: data.service.id },
      status: BookingStatus.ACTIVE,
      extra: IsNull(),
    };
    if (data.extra) {
      if (data.service.category.type == CategoryType.CAFE) {
        where.extra = JsonContains({ table: data.extra.table });
      }
    }
    const exists = await this.repo.exists({
      where,
      relations: BOOKING_RELATIONS,
    });
    this.logger.log(`${BookingService.name} isBusy end`);
    return exists;
  }
}
