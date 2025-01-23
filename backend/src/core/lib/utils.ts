import { WeekDay } from '@shared/interfaces';
import * as crypto from 'crypto';
import * as fs from 'fs';
import { Duplex } from 'stream';

export const createHash = (value: string, key: string) => {
  const hashValue = value + key;
  return crypto.createHash('sha256').update(hashValue).digest('hex');
};

export const randomString = (len: number, charSet = '') => {
  charSet =
    charSet || 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let randomString = '';
  for (let i = 0; i < len; i++) {
    const randomPoz = Math.floor(Math.random() * charSet.length);
    randomString += charSet.substring(randomPoz, randomPoz + 1);
  }
  return randomString;
};

export const saveFile = (
  path: string,
  data: string | NodeJS.ArrayBufferView,
  encoding: fs.WriteFileOptions,
) => {
  return new Promise((resolve, reject) => {
    fs.writeFile(path, data, encoding, (error) => {
      if (error) {
        reject(error);
      } else {
        resolve(path);
      }
    });
  });
};

export const removeFile = (path: string) => {
  fs.unlink(path, (error) => {
    if (error) {
      console.log('Error remove file', error);
    }
  });
};

export const getFileExtensionByName = (name: string) => {
  const split = name.split('.');
  return split.length > 1 ? split[split.length - 1] : '';
};

export const bufferToStream = (buffer) => {
  const stream = new Duplex();
  stream.push(buffer);
  stream.push(null);
  return stream;
};

export const getFile = (
  path: string,
  options?: {
    encoding?: null | undefined;
    flag?: string | undefined;
  } | null,
) => {
  if (!path) {
    throw new Error('Error An empty required parameter was passed: path');
  }
  if (options) {
    return fs.readFileSync(path, options);
  }
  return fs.readFileSync(path);
};

export const calculateDistance = (p1: any, p2: any) => {
  const lat = p1.latitude - p2.latitude;
  const lon = p1.longitude - p2.longitude;
  return Math.sqrt(lon * lon - lat * lat);
};

export const getWeekDay = (day: number) => {
  switch (day) {
    case 0:
      return WeekDay.SUNDAY;
    case 1:
      return WeekDay.MONDAY;
    case 2:
      return WeekDay.TUESDAY;
    case 3:
      return WeekDay.WEDNESDAY;
    case 4:
      return WeekDay.THURSDAY;
    case 5:
      return WeekDay.FRIDAY;
    case 6:
      return WeekDay.SATURDAY;
  }
  return null;
};
