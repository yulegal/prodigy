import { Injectable, Logger } from '@nestjs/common';
import { EventEmitter2 } from '@nestjs/event-emitter';

@Injectable()
export class GlobalCronEmitterService {
  private logger: Logger;

  constructor(private readonly emitter: EventEmitter2) {
    this.logger = new Logger();
  }
}
