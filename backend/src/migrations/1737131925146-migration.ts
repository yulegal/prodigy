import { MigrationInterface, QueryRunner } from 'typeorm';

export class Migration1737131925146 implements MigrationInterface {
  public async up(queryRunner: QueryRunner): Promise<void> {
    queryRunner.query(`
            insert into "roles"("id", "name") values('529b2630-8905-4f47-a46c-6b58f9aef84a', 'Admin');
            insert into "roles"("id", "name") values('529b2630-8905-4f47-a46c-6b58f9aef84b', 'User');
            insert into "roles"("id", "name") values('529b2630-8905-4f47-a46c-6b58f9aef84c', 'Provider');
            insert into "roles"("id", "name") values('529b2630-8905-4f47-a46c-6b58f9aef84d', 'Helper');
            insert into "users"("id", "roleId", "name", "login", "key", "hash", "locale") values('529b2630-8905-4f47-a46c-6b58f9aef84a', '529b2630-8905-4f47-a46c-6b58f9aef84a', 'Supervisor', 'Supervisor', '8492d113c9a25b2d0d76', '8de1322e4825bf5777ad0d860fb897174e252f61efbc775f1dc2950cec0e9de9', 'en');
        `);
  }

  //eslint-disable-next-line @typescript-eslint/no-unused-vars
  public async down(queryRunner: QueryRunner): Promise<void> {}
}
