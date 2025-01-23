import { Module } from '@nestjs/common';
import { UserController } from './user.controller';
import { UserService } from './user.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UserEnitity } from '@/entities/UserEntity';
import { JwtModule } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import { RoleEntity } from '@/entities/RoleEntity';
import { ConfigModule } from '@/core/config/config.module';
import { ContactEntity } from '@/entities/ContactEntity';

@Module({
  controllers: [UserController],
  providers: [UserService],
  exports: [UserService],
  imports: [
    TypeOrmModule.forFeature([UserEnitity, RoleEntity, ContactEntity]),
    JwtModule.registerAsync({
      useFactory: (config: ConfigService) => ({
        signOptions: {
          expiresIn: config.get<string>('TOKEN_EXPIRES'),
        },
        secret: config.get<string>('SECRET_KEY'),
      }),
      inject: [ConfigService],
      extraProviders: [ConfigService],
    }),
    ConfigModule,
  ],
})
export class UserModule {}
