import { Module } from '@nestjs/common';
import { GlobalCronService } from './global-cron.service';
import { GlobalCronEmitterService } from './global-cron-emitter.service';

@Module({
  providers: [GlobalCronService, GlobalCronEmitterService],
  exports: [GlobalCronService, GlobalCronEmitterService],
})
export class GlobalCronModule {}
