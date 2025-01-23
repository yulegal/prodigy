import * as rootPath from 'app-root-path';
import fs from 'fs';
import path from 'path';
import os from 'os';

const ROOT_PATH = rootPath.path;
const config: Record<string, any> = {};
let msFilesPath = '';
let publicFilesPath = '';
let filesPath = '';
let addonsFilesPath = '';

const initFileCatalog = () => {
  try {
    if (os.platform() == 'linux') {
      msFilesPath = '/msdata';
    } else {
      msFilesPath = path.join(ROOT_PATH, '..', 'msdata');
    }
    if (!fs.existsSync(msFilesPath)) fs.mkdirSync(msFilesPath);
  } catch (e) {
    console.error(
      `Failed to open or create directory for the service: ${msFilesPath}`,
    );
    throw e;
  }

  filesPath = path.join(msFilesPath, 'files');
  try {
    if (!fs.existsSync(filesPath)) fs.mkdirSync(filesPath);
  } catch (e) {
    console.error(
      `Failed to open or create directory for the service: ${filesPath}`,
    );
    throw e;
  }

  publicFilesPath = path.join(filesPath, 'public');
  try {
    if (!fs.existsSync(publicFilesPath)) fs.mkdirSync(publicFilesPath);
  } catch (e) {
    console.error(
      `Failed to open or create directory for the service: ${publicFilesPath}`,
    );
    throw e;
  }

  addonsFilesPath = path.join(filesPath, 'addons');
  try {
    if (!fs.existsSync(addonsFilesPath)) fs.mkdirSync(addonsFilesPath);
  } catch (e) {
    console.error(
      `Failed to open or create directory for the service: ${addonsFilesPath}`,
    );
    throw e;
  }
};

const init = function () {
  try {
    initFileCatalog();
  } catch (e) {
    throw new Error(`Configuration initialization error:  ${e.toString()}`);
  }
};

export {
  config,
  publicFilesPath as PUBLIC_FILES_PATH,
  init,
  addonsFilesPath as ADDONS_FILES_PATH,
};
