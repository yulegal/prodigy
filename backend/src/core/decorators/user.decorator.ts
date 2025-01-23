import { createParamDecorator } from '@nestjs/common';
import { ExecutionContextHost } from '@nestjs/core/helpers/execution-context-host';

export const User = createParamDecorator(
  (data: any, context: ExecutionContextHost) => {
    const host = context.switchToHttp().getRequest();
    return host.user;
  },
);
