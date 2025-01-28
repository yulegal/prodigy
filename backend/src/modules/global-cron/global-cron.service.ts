import { Injectable, Logger } from '@nestjs/common';
import { OnEvent } from '@nestjs/event-emitter';
import { GLOBAL_CRON_EVENTS } from './global-cron.enum';
import { Between, LessThan, MoreThanOrEqual, Repository } from 'typeorm';
import { BookingEntity } from '@/entities/BookingEntity';
import moment from 'moment';
import { BookingStatus } from '@shared/interfaces/booking';
import { InjectRepository } from '@nestjs/typeorm';
import { ServiceEntity } from '@/entities/ServiceEntity';
import { NotificationEntity } from '@/entities/NotificationEntity';
import { NotificationType } from '@shared/interfaces/notification';
import { SERVICE_RELATIONS } from '@/core/defs';
import { NotificationService } from '../notification/notification.service';
import {
  BALANCE_FEE_CHARGED_NOTIFICATION,
  PAYMENT_DATE_APPROACH_NOTIFICATION,
  SERVICE_BLOCKED_NOTIFICATIONS,
  TRIAL_PERIOD_APPROACHES_NOTIFICATION,
} from '@/core/translations';
import { UserEnitity } from '@/entities/UserEntity';

@Injectable()
export class GlobalCronService {
  private logger: Logger;

  constructor(
    @InjectRepository(BookingEntity)
    private readonly bookingRepo: Repository<BookingEntity>,
    @InjectRepository(ServiceEntity)
    private readonly serviceRepo: Repository<ServiceEntity>,
    @InjectRepository(NotificationEntity)
    private readonly notificationRepo: Repository<NotificationEntity>,
    private readonly notificationService: NotificationService,
    @InjectRepository(UserEnitity)
    private readonly userRepo: Repository<UserEnitity>,
  ) {
    this.logger = new Logger(GlobalCronService.name);
  }

  @OnEvent(GLOBAL_CRON_EVENTS.HANDLE_BOOKINGS)
  async handleBookings() {
    this.logger.log(`${GlobalCronService.name} handleBookings start`);
    await this.handleStaleActiveBookings();
    this.logger.log(`${GlobalCronService.name} handleBookings end`);
  }

  private async handleStaleActiveBookings() {
    this.logger.log(
      `${GlobalCronService.name} handleStaleActiveBookings start`,
    );
    const m = moment();
    const bookings = await this.bookingRepo.find({
      where: {
        bookDate: LessThan(m.toDate()),
        status: BookingStatus.ACTIVE,
      },
    });
    for (const booking of bookings) {
      booking.status = BookingStatus.DONE;
    }
    await this.bookingRepo.save(bookings);
    this.logger.log(`${GlobalCronService.name} handleStaleActiveBookings end`);
  }

  @OnEvent(GLOBAL_CRON_EVENTS.HANDLE_PAYMENT_APPROACH)
  async handlePaymentApproach() {
    this.logger.log(`${GlobalCronService.name} handlePaymentApproach start`);
    const m = moment();
    const services = await this.serviceRepo.find({
      where: {
        paymentEndDate: Between(m.toDate(), m.add(3, 'days').toDate()),
        blocked: false,
      },
      relations: SERVICE_RELATIONS,
    });
    for (const service of services) {
      const pd = moment(service.paymentEndDate);
      const exists = await this.notificationRepo.exists({
        where: {
          user: { id: service.user.id },
          createdAt: Between(m.toDate(), pd.toDate()),
          type: NotificationType.PAYMENT_PERIOD_APPROACHES,
        },
      });
      if (!exists) {
        const date = pd.format('DD.MM.YYYY');
        await this.notificationService.create({
          title: PAYMENT_DATE_APPROACH_NOTIFICATION.title[service.user.locale],
          body: PAYMENT_DATE_APPROACH_NOTIFICATION.body[
            service.user.locale
          ].replace('%date', date),
          type: NotificationType.PAYMENT_PERIOD_APPROACHES,
          userId: service.user.id,
        });
      }
    }
    this.logger.log(`${GlobalCronService.name} handlePaymentApproach end`);
  }

  @OnEvent(GLOBAL_CRON_EVENTS.HANDLE_TRIALS)
  async handleTrials() {
    this.logger.log(`${GlobalCronService.name} handleTrials start`);
    const m = moment();
    const services = await this.serviceRepo.find({
      where: {
        blocked: false,
        trialEndDate: LessThan(m.toDate()),
      },
      relations: SERVICE_RELATIONS,
    });
    for (const service of services) {
      service.blocked = true;
      await this.notificationService.create({
        title: SERVICE_BLOCKED_NOTIFICATIONS.title[service.user.locale],
        body: SERVICE_BLOCKED_NOTIFICATIONS.body[service.user.locale],
        type: NotificationType.SERVICE_BLOCKED_DUE_TO_LACK_BALANCE,
        userId: service.user.id,
      });
    }
    await this.serviceRepo.save(services);
    this.logger.log(`${GlobalCronService.name} handleTrials end`);
  }

  @OnEvent(GLOBAL_CRON_EVENTS.HANDLE_TRIAL_APPROACH)
  async handleTrialApproach() {
    this.logger.log(`${GlobalCronService.name} handleTrialApproach start`);
    const m = moment();
    const services = await this.serviceRepo.find({
      where: {
        blocked: false,
        trialEndDate: Between(m.toDate(), m.add(3, 'days').toDate()),
      },
      relations: SERVICE_RELATIONS,
    });
    for (const service of services) {
      const td = moment(service.trialEndDate);
      const exists = await this.notificationRepo.exists({
        where: {
          user: { id: service.user.id },
          createdAt: Between(m.toDate(), td.toDate()),
          type: NotificationType.TRIAL_PERIOD_END_APPROACHES,
        },
      });
      if (!exists) {
        const date = td.format('DD.MM.YYYY');
        await this.notificationService.create({
          title:
            TRIAL_PERIOD_APPROACHES_NOTIFICATION.title[service.user.locale],
          body: TRIAL_PERIOD_APPROACHES_NOTIFICATION.body[
            service.user.locale
          ].replace('%date', date),
          type: NotificationType.TRIAL_PERIOD_END_APPROACHES,
          userId: service.user.id,
        });
      }
    }
    this.logger.log(`${GlobalCronService.name} handleTrialApproach end`);
  }

  @OnEvent(GLOBAL_CRON_EVENTS.HANDLE_PAYMENTS)
  async handlePayments() {
    this.logger.log(`${GlobalCronService.name} handlePayments start`);
    const m = moment();
    const services = await this.serviceRepo.find({
      where: {
        blocked: false,
        user: {
          balance: MoreThanOrEqual(0),
        },
        paymentEndDate: LessThan(m.toDate()),
      },
      relations: SERVICE_RELATIONS,
    });
    const users: UserEnitity[] = [];
    for (const service of services) {
      const t = moment(service.paymentEndDate).add(1, 'month');
      if (service.user.balance < service.feePerMonth) {
        service.blocked = true;
        await this.notificationService.create({
          title: SERVICE_BLOCKED_NOTIFICATIONS.title[service.user.locale],
          body: SERVICE_BLOCKED_NOTIFICATIONS.body[service.user.locale],
          type: NotificationType.SERVICE_BLOCKED_DUE_TO_LACK_BALANCE,
          userId: service.user.id,
        });
      } else if (!m.isBetween(service.paymentEndDate, t.toDate())) {
        const user = await this.userRepo.findOne({
          where: { id: service.user.id },
        });
        user.balance -= service.feePerMonth;
        users.push(user);
        await this.notificationService.create({
          title: BALANCE_FEE_CHARGED_NOTIFICATION.title[service.user.locale],
          body: BALANCE_FEE_CHARGED_NOTIFICATION.body[
            service.user.locale
          ].replace('%fee', service.feePerMonth.toString()),
          type: NotificationType.FEE_CHARGED,
          userId: service.user.id,
        });
        service.paymentEndDate = t.toDate();
      }
    }
    await this.serviceRepo.save(services);
    if (users.length) {
      await this.userRepo.save(users);
    }
    this.logger.log(`${GlobalCronService.name} handlePayments end`);
  }
}
