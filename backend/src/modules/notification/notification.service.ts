import { FilterQueryOptions } from '@/core/dto';
import { NotificationEntity } from '@/entities/NotificationEntity';
import { UserEnitity } from '@/entities/UserEntity';
import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  Logger,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Between, FindOptionsWhere, Repository } from 'typeorm';
import { UserDto } from '../user/dto';
import { getPageableData } from '@/core/db-extension';
import { NotificationMapper } from './mapper';
import {
  BOOKING_RELATIONS,
  NOTIFICATION_RELATIONS,
  USER_RELATIONS,
} from '@/core/defs';
import { BroadcastCreateDto, NotificationCreateDto } from './dto';
import { SocketService } from '@/core/socket/socket.service';
import {
  error_empty_bookings,
  error_notification_not_found,
} from '@shared/errors';
import { BookingEntity } from '@/entities/BookingEntity';
import moment from 'moment';
import {
  BROADCAST_ACTION,
  NotificationType,
} from '@shared/interfaces/notification';
import { BROADCAST_CANCEL_NOTIFICATIONS } from '@/core/translations';

@Injectable()
export class NotificationService {
  private logger: Logger;

  constructor(
    @InjectRepository(NotificationEntity)
    private readonly repo: Repository<NotificationEntity>,
    @InjectRepository(UserEnitity)
    private readonly userRepo: Repository<UserEnitity>,
    private readonly socketService: SocketService,
    @InjectRepository(BookingEntity)
    private readonly bookingRepo: Repository<BookingEntity>,
  ) {
    this.logger = new Logger(NotificationService.name);
  }

  async create(dto: NotificationCreateDto) {
    this.logger.log(`${NotificationService.name} create start`);
    const notification = new NotificationEntity();
    notification.user = await this.userRepo.findOne({
      where: { id: dto.userId },
      relations: USER_RELATIONS,
    });
    notification.title = dto.title;
    notification.body = dto.body;
    notification.type = dto.type;
    if (dto.itemId) notification.itemId = dto.itemId;
    const result = await this.repo.save(notification);
    const mapped = NotificationMapper.map(result);
    await this.socketService.newNotification(mapped);
    this.logger.log(`${NotificationService.name} create end`);
    return mapped;
  }

  async filter(options: FilterQueryOptions, currentUser: UserDto) {
    this.logger.log(`${NotificationService.name} filter start`);
    const where: FindOptionsWhere<NotificationEntity> = {
      user: { id: currentUser.id },
    };
    return getPageableData(
      this.repo,
      options,
      NotificationMapper.mapList,
      NOTIFICATION_RELATIONS,
      where,
    );
  }

  async getCount(currentUser: UserDto) {
    this.logger.log(`${NotificationService.name} getCount start`);
    const count = await this.repo.count({
      where: {
        user: { id: currentUser.id },
        isRead: false,
      },
    });
    this.logger.log(`${NotificationService.name} getCount end`);
    return count;
  }

  async read(id: string, currentUser: UserDto) {
    this.logger.log(`${NotificationService.name} read start`);
    const notification = await this.repo.findOne({
      where: { id },
      relations: NOTIFICATION_RELATIONS,
    });
    if (!notification)
      throw new BadRequestException(error_notification_not_found);
    if (notification.user.id != currentUser.id) throw new ForbiddenException();
    notification.isRead = true;
    await this.repo.save(notification);
    this.logger.log(`${NotificationService.name} read end`);
    return true;
  }

  async broadcast(dto: BroadcastCreateDto, currentUser: UserDto) {
    this.logger.log(`${NotificationService.name} broadcast start`);
    const where: FindOptionsWhere<BookingEntity> = {
      bookDate: Between(
        moment(dto.date).startOf('day').toDate(),
        moment(dto.date).endOf('day').toDate(),
      ),
      service: {
        user: { id: currentUser.id },
      },
    };
    if (dto.branchId) {
      where.branch = { id: dto.branchId };
    }
    const bookings = await this.bookingRepo.find({
      where,
      relations: BOOKING_RELATIONS,
    });
    if (!bookings.length) throw new BadRequestException(error_empty_bookings);
    for (const booking of bookings) {
      let title = '';
      let body = '';
      switch (dto.action) {
        case BROADCAST_ACTION.CANCEL_BOOKING:
          const date = moment(dto.date).format('DD.MM.YYYY HH:mm');
          title = BROADCAST_CANCEL_NOTIFICATIONS.title[booking.user.locale]
            .replace('%date', date)
            .replace('%name', currentUser.name);
          body = dto.message;
          break;
      }
      await this.create({
        type: NotificationType.NEW_BROADCAST,
        title,
        body,
        itemId: booking.service.user.id,
        userId: booking.user.id,
      });
    }
    switch (dto.action) {
      case BROADCAST_ACTION.CANCEL_BOOKING:
        await this.bookingRepo.remove(bookings);
        break;
    }
    this.logger.log(`${NotificationService.name} broadcast end`);
    return true;
  }
}
