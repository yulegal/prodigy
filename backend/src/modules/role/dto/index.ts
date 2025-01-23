import { PageableResponseData } from '@/core/dto';
import { IRole, IRolePageableResponseData } from '@shared/interfaces/role';

export class RoleDto implements IRole {
  id: string;
  name: string;
}

export class RolePageableResponseData
  extends PageableResponseData
  implements IRolePageableResponseData
{
  data: RoleDto | RoleDto[];
}
