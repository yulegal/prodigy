import { Injectable, Logger } from '@nestjs/common';
import { EventEmitter2 } from '@nestjs/event-emitter';
import { Cron, CronExpression } from '@nestjs/schedule';
import { GLOBAL_CRON_EVENTS } from './global-cron.enum';

@Injectable()
export class GlobalCronEmitterService {
  private logger: Logger;

  constructor(private readonly emitter: EventEmitter2) {
    this.logger = new Logger();
  }

  @Cron(CronExpression.EVERY_5_MINUTES)
  async handleBookings() {
    this.logger.log(`${GlobalCronEmitterService.name} handleBookings start`);
    this.emitter.emit(GLOBAL_CRON_EVENTS.HANDLE_BOOKINGS);
    this.logger.log(`${GlobalCronEmitterService.name} handleBookings end`);
  }

  @Cron(CronExpression.EVERY_10_MINUTES)
  async handlePaymentApproach() {
    this.logger.log(`${GlobalCronEmitterService.name} handlePaymentApproach start`);
    this.emitter.emit(GLOBAL_CRON_EVENTS.HANDLE_PAYMENT_APPROACH);
    this.logger.log(`${GlobalCronEmitterService.name} handlePaymentApproach end`);
  }

  @Cron(CronExpression.EVERY_10_MINUTES)
  async handleTrials() {
    this.logger.log(`${GlobalCronEmitterService.name} handleTrials start`);
    this.emitter.emit(GLOBAL_CRON_EVENTS.HANDLE_TRIALS);
    this.logger.log(`${GlobalCronEmitterService.name} handleTrials end`);
  }

  @Cron(CronExpression.EVERY_10_MINUTES)
  async handleTrialApproach() {
    this.logger.log(`${GlobalCronEmitterService.name} handleTrialApproach start`);
    this.emitter.emit(GLOBAL_CRON_EVENTS.HANDLE_TRIAL_APPROACH);
    this.logger.log(`${GlobalCronEmitterService.name} handleTrialApproach end`);
  }

  @Cron(CronExpression.EVERY_10_MINUTES)
  async handlePayments() {
    this.logger.log(`${GlobalCronEmitterService.name} handlePayments start`);
    this.emitter.emit(GLOBAL_CRON_EVENTS.HANDLE_PAYMENTS);
    this.logger.log(`${GlobalCronEmitterService.name} handlePayments end`);
  }
}
