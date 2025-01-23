import { Injectable, Logger } from '@nestjs/common';

@Injectable()
export class GlobalCronService {
  private logger: Logger;

  constructor() {
    this.logger = new Logger(GlobalCronService.name);
  }
}
