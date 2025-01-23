import { Body, Controller, Post, UseGuards } from '@nestjs/common';
import { RoleService } from './role.service';
import { RolesGuard } from '@/core/guards/roles.guard';
import { FilterQueryOptions } from '@/core/dto';
import { Roles } from '@/core/decorators/role.decorator';
import { USER_ROLES } from '@/core/defs';

@Controller({
  path: 'role',
})
export class RoleController {
  constructor(private readonly service: RoleService) {}

  @Post('filter')
  @Roles(USER_ROLES.admin)
  @UseGuards(RolesGuard)
  async filter(@Body() options: FilterQueryOptions) {
    return this.service.filter(options);
  }
}
