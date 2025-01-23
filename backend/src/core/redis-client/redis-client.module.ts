import { Global, Module } from '@nestjs/common';
import { RedisClientService } from './redis-client.service';
import { ConfigModule } from '../config/config.module';

@Global()
@Module({
  providers: [RedisClientService],
  exports: [RedisClientService],
  imports: [ConfigModule],
})
export class RedisClientModule {}
