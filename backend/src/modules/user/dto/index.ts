import { PageableResponseData } from '@/core/dto';
import { RoleDto } from '@/modules/role/dto';
import { Locale } from '@shared/interfaces';
import {
  IUser,
  IUserContact,
  IUserCreate,
  IUserEditName,
  IUserHandleContact,
  IUserLogin,
  IUserPageableResponseData,
  UserSettings,
} from '@shared/interfaces/user';

export class UserDto implements IUser {
  id: string;
  name: string;
  login: string;
  createdAt: number;
  updatedAt: number;
  isVerified: boolean;
  locale: Locale;
  role: RoleDto;
  icon?: string;
  balance?: number;
  settings?: UserSettings;
  status?: string;
}

export class UserLoginDto implements IUserLogin {
  login: string;
}

export class UserCreateDto extends UserLoginDto implements IUserCreate {
  name: string;
  locale: Locale;
}

export class UserEditNameDto implements IUserEditName {
  name: string;
}

export class UserContactDto implements IUserContact {
  name: string;
  number: string;
}

export class UserHandleContactDto implements IUserHandleContact {
  data: UserContactDto[];
}

export class UserPageableResponseData
  extends PageableResponseData
  implements IUserPageableResponseData
{
  data: UserDto | UserDto[];
}
