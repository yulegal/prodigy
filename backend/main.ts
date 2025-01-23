import { NestFactory } from '@nestjs/core';
import { AppModule } from './src/app.module';
import * as dotenv from 'dotenv';
import * as path from 'path';
import { json, urlencoded } from 'body-parser';
import * as config from '@/core/lib/config';

dotenv.config({ path: path.join(process.cwd(), '.env') });

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  app.enableCors({
    origin: process.env.HTTP_HOST.split(','),
    credentials: true,
  });
  app.use(json());
  app.setGlobalPrefix('/api');
  app.use(urlencoded({ extended: true }));
  config.init();
  await app.listen(process.env.HTTP_PORT);
}
bootstrap();
