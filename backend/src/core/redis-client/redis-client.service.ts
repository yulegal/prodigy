import { Injectable, Logger } from '@nestjs/common';
import { Redis, RedisOptions } from 'ioredis';
import { ConfigService } from '../config/config.service';

@Injectable()
export class RedisClientService {
  private client: Redis;
  private logger = new Logger(RedisClientService.name);

  constructor(private readonly configService: ConfigService) {}

  async init() {
    const maxRetriesPerRequest = 20;

    const options: RedisOptions = {
      host: process.env.REDIS_HOST,
      port: Number(process.env.REDIS_PORT),
      keyPrefix: '',
      maxRetriesPerRequest,
      retryStrategy: (times: number) => {
        if (times > maxRetriesPerRequest) {
          this.logger.error(
            `ERROR: Redis connection could not be established after ${maxRetriesPerRequest} attempts`,
          );
          return undefined;
        }
        return Math.min(times * 100, 1000);
      },
    };

    this.client = new Redis(options);

    this.client.on('error', (error) => {
      this.logger.error(`Redis Error: ${error}`);
    });

    this.client.on('ready', () => {
      this.logger.log('Redis is ready !');
    });

    this.client.on('connect', () => {
      this.logger.log('Connect to redis success !');
    });
  }

  public async set(key: string, value: string, seconds?: number) {
    value = JSON.stringify(value);
    const client = await this.getClient();
    if (seconds) await client.set(key, value, 'EX', seconds);
    else await client.set(key, value);
  }

  public async get(key: string) {
    const client = await this.getClient();
    const value = await client.get(key);
    if (value) return JSON.parse(value);
    else return null;
  }

  public async del(key: string) {
    const client = await this.getClient();
    await client.del(key);
  }

  public async flushall() {
    const client = await this.getClient();
    await client.flushall();
  }

  public async getClient() {
    if (!this.client) await this.init();
    return this.client;
  }

  public async getAll(pattern = '*') {
    const client = await this.getClient();
    const keys = await client.keys(pattern);

    const items = [];
    for (const key of keys) {
      const item = {
        key: key,
        value: await this.get(key),
      };
      items.push(item);
    }
    return items;
  }
}
