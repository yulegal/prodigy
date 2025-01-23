import { Injectable } from '@nestjs/common';
import * as config from '../lib/config';

@Injectable()
export class ConfigService {
  get<T>(confName: string) {
    try {
      const items = confName.split('.');
      let value = config.config;
      for (const item of items) value = value[item];
      return value as T;
    } catch (err) {
      throw new Error('Could not fetch configuration ' + confName);
    }
  }

  getPublicFilesPath() {
    return config.PUBLIC_FILES_PATH;
  }

  getAddonsFilesPath() {
    return config.ADDONS_FILES_PATH;
  }
}
