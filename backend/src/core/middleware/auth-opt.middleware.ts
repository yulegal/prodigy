import { Inject, Injectable, NestMiddleware } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { NextFunction, Response } from 'express';

@Injectable()
export class AuthOptMiddleware implements NestMiddleware {
  constructor(
    @Inject(ConfigService)
    private readonly configService: ConfigService,
    @Inject(JwtService)
    private readonly jwtService: JwtService,
  ) {}

  async use(req: any, res: Response, next: NextFunction) {
    const authorization =
      req.headers['Authorization'] || req.headers['authorization'];
    if (authorization) {
      const token = authorization.split(' ')[1];
      if (token) {
        try {
          const user = await this.jwtService.verifyAsync(token, {
            secret: this.configService.get<string>('SECRET_KEY'),
          });
          req.user = user;
        } catch (e) {}
      }
    }
    next();
  }
}
