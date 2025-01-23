import { Module } from '@nestjs/common';
import { WsGateway } from './ws.gateways';

@Module({
  providers: [WsGateway],
})
export class WsGatewayModule {}
