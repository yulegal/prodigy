import { Global, Module } from '@nestjs/common';
import { SocketService } from './socket.service';
import { RedisClientModule } from '../redis-client/redis-client.module';

@Global()
@Module({
  providers: [SocketService],
  exports: [SocketService],
  imports: [RedisClientModule],
})
export class SocketModule {}
