import { ConfigService } from '@/core/config/config.service';
import {
  BadRequestException,
  Controller,
  Get,
  Param,
  Res,
} from '@nestjs/common';
import { Response } from 'express';

@Controller({
  path: 'public_files',
})
export class PublicController {
  constructor(private readonly configService: ConfigService) {}

  @Get(':file')
  async getFile(@Param('file') file: string, @Res() res: Response) {
    if (!file) throw new BadRequestException('Empty param filename');
    const fp = this.configService.getPublicFilesPath() + '/' + file;
    return res.sendFile(fp);
  }

  @Get('addon/:file')
  async getAddonFile(@Param('file') file: string, @Res() res: Response) {
    if (!file) throw new BadRequestException('Empty param filename');
    const fp = this.configService.getAddonsFilesPath() + '/' + file;
    return res.sendFile(fp);
  }
}
