import { MigrationInterface, QueryRunner } from 'typeorm';

export class Migration1737131867489 implements MigrationInterface {
  name = 'Migration1737131867489';

  public async up(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.query(
      `CREATE TABLE "roles" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "name" character varying(100) NOT NULL, CONSTRAINT "PK_c1433d71a4838793a49dcad46ab" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TYPE "public"."users_locale_enum" AS ENUM('en', 'ru')`,
    );
    await queryRunner.query(
      `CREATE TABLE "users" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "name" character varying(60) NOT NULL, "login" character varying(100) NOT NULL, "verifiedAt" TIMESTAMP, "hash" character varying(300), "key" character varying(300), "locale" "public"."users_locale_enum" NOT NULL, "icon" character varying, "balance" integer, "settings" json, "roleId" uuid NOT NULL, CONSTRAINT "UQ_2d443082eccd5198f95f2a36e2c" UNIQUE ("login"), CONSTRAINT "PK_a3ffb1c0c8416b9fc6f907b7433" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TYPE "public"."categories_type_enum" AS ENUM('HAIRCUT', 'CAFE')`,
    );
    await queryRunner.query(
      `CREATE TABLE "categories" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "name" json NOT NULL, "icon" character varying, "type" "public"."categories_type_enum" NOT NULL, CONSTRAINT "PK_24dbc6126a28ff948da33e97d3b" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TYPE "public"."services_unit_enum" AS ENUM('hours', 'minutes')`,
    );
    await queryRunner.query(
      `CREATE TABLE "services" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "extra" json, "averageSession" integer NOT NULL, "unit" "public"."services_unit_enum" NOT NULL, "address" json NOT NULL, "workSchedule" json NOT NULL, "rating" integer, "name" character varying(150) NOT NULL, "blocked" boolean NOT NULL DEFAULT false, "paymentEndDate" TIMESTAMP, "trialEndDate" TIMESTAMP, "feePerMonth" integer, "icon" character varying(200), "userId" uuid, "categoryId" uuid, CONSTRAINT "PK_ba2d347a3168a296416c6c5ccb2" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TABLE "branch_users" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "userId" uuid, "branchId" uuid, CONSTRAINT "PK_165d04e1f7360e289b9afa2ee96" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TYPE "public"."branches_unit_enum" AS ENUM('hours', 'minutes')`,
    );
    await queryRunner.query(
      `CREATE TABLE "branches" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "extra" json, "averageSession" integer NOT NULL, "unit" "public"."branches_unit_enum" NOT NULL, "address" json NOT NULL, "workSchedule" json NOT NULL, "rating" integer, "serviceId" uuid, CONSTRAINT "PK_7f37d3b42defea97f1df0d19535" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TYPE "public"."bookings_status_enum" AS ENUM('ACTIVE', 'CANCELED', 'DONE')`,
    );
    await queryRunner.query(
      `CREATE TABLE "bookings" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "bookDate" TIMESTAMP NOT NULL, "status" "public"."bookings_status_enum" NOT NULL, "extra" json, "notes" character varying(150), "serviceId" uuid, "userId" uuid, "branchId" uuid, CONSTRAINT "PK_bee6805982cc1e248e94ce94957" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TABLE "favorites" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "userId" uuid, "serviceId" uuid, CONSTRAINT "PK_890818d27523748dd36a4d1bdc8" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TABLE "galleries" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "photo" character varying(200) NOT NULL, "serviceId" uuid, "branchId" uuid, CONSTRAINT "PK_86b77299615c92db3d68c9c7919" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TYPE "public"."notifications_type_enum" AS ENUM('NEW_BOOKING', 'BOOKING_CANCELED', 'USER_ADDED_TO_BRANCH', 'USER_REMOVED_FROM_BRANCH', 'REBOOKED', 'FEE_CHARGED', 'PAYMENT_PERIOD_APPROACHES', 'SERVICE_BLOCKED_DUE_TO_LACK_BALANCE', 'TRIAL_PERIOD_END_APPROACHES', 'BOOKING_FINISHED')`,
    );
    await queryRunner.query(
      `CREATE TABLE "notifications" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "title" character varying(100) NOT NULL, "body" character varying(250) NOT NULL, "isRead" boolean NOT NULL DEFAULT false, "type" "public"."notifications_type_enum" NOT NULL, "itemId" uuid, "userId" uuid, CONSTRAINT "PK_6a72c3c0f683f6462415e653c3a" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `ALTER TABLE "users" ADD CONSTRAINT "FK_368e146b785b574f42ae9e53d5e" FOREIGN KEY ("roleId") REFERENCES "roles"("id") ON DELETE NO ACTION ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "services" ADD CONSTRAINT "FK_3905389899d96c4f1b3619f68d5" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "services" ADD CONSTRAINT "FK_034b52310c2d211bc979c3cc4e8" FOREIGN KEY ("categoryId") REFERENCES "categories"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "branch_users" ADD CONSTRAINT "FK_c0d1e91764bfdafe504ed8b3717" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "branch_users" ADD CONSTRAINT "FK_e5d6aa8cf1ebfdcc242580160cb" FOREIGN KEY ("branchId") REFERENCES "branches"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "branches" ADD CONSTRAINT "FK_1d08996d9003bfdc86cefe01dee" FOREIGN KEY ("serviceId") REFERENCES "services"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "bookings" ADD CONSTRAINT "FK_15a2431ec10d29dcd96c9563b65" FOREIGN KEY ("serviceId") REFERENCES "services"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "bookings" ADD CONSTRAINT "FK_38a69a58a323647f2e75eb994de" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "bookings" ADD CONSTRAINT "FK_64de318a01c502530b1e32692fd" FOREIGN KEY ("branchId") REFERENCES "branches"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "favorites" ADD CONSTRAINT "FK_e747534006c6e3c2f09939da60f" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "favorites" ADD CONSTRAINT "FK_307c40c36b91a85aea3f22c20d6" FOREIGN KEY ("serviceId") REFERENCES "services"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "galleries" ADD CONSTRAINT "FK_797bb7d4e10c84afcfadaca1d18" FOREIGN KEY ("serviceId") REFERENCES "services"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "galleries" ADD CONSTRAINT "FK_e9d0c45df9bf38474ed6cbdd291" FOREIGN KEY ("branchId") REFERENCES "branches"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "notifications" ADD CONSTRAINT "FK_692a909ee0fa9383e7859f9b406" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `CREATE TABLE "service_ratings" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "rating" integer NOT NULL, "userId" uuid, "serviceId" uuid, CONSTRAINT "PK_8969d63e24f8d58c96952fa1346" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TABLE "branch_ratings" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "rating" integer NOT NULL, "userId" uuid, "branchId" uuid, CONSTRAINT "PK_e9124a7b1aaeceef59fdd668611" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(`ALTER TABLE "services" DROP COLUMN "rating"`);
    await queryRunner.query(`ALTER TABLE "branches" DROP COLUMN "rating"`);
    await queryRunner.query(
      `ALTER TABLE "service_ratings" ADD CONSTRAINT "FK_5767e6dfeeaa708984a33426fc4" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "service_ratings" ADD CONSTRAINT "FK_58849e8c0ea7fd3f8892836c1d7" FOREIGN KEY ("serviceId") REFERENCES "services"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "branch_ratings" ADD CONSTRAINT "FK_5e2f9fa771c014e0f412d7ae28d" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "branch_ratings" ADD CONSTRAINT "FK_eac2bfe2665ab444c4b2e3edd55" FOREIGN KEY ("branchId") REFERENCES "branches"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TYPE "public"."notifications_type_enum" RENAME TO "notifications_type_enum_old"`,
    );
    await queryRunner.query(
      `CREATE TYPE "public"."notifications_type_enum" AS ENUM('NEW_BOOKING', 'BOOKING_CANCELED', 'USER_ADDED_TO_BRANCH', 'USER_REMOVED_FROM_BRANCH', 'REBOOKED', 'FEE_CHARGED', 'PAYMENT_PERIOD_APPROACHES', 'SERVICE_BLOCKED_DUE_TO_LACK_BALANCE', 'TRIAL_PERIOD_END_APPROACHES', 'BOOKING_FINISHED', 'NEW_BROADCAST')`,
    );
    await queryRunner.query(
      `ALTER TABLE "notifications" ALTER COLUMN "type" TYPE "public"."notifications_type_enum" USING "type"::"text"::"public"."notifications_type_enum"`,
    );
    await queryRunner.query(`DROP TYPE "public"."notifications_type_enum_old"`);
    await queryRunner.query(
      `CREATE TABLE "chats" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "user1Id" uuid, "user2Id" uuid, CONSTRAINT "PK_0117647b3c4a4e5ff198aeb6206" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TABLE "contacts" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "userId" uuid, "contactId" uuid, CONSTRAINT "PK_b99cd40cfd66a99f1571f4f72e6" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `CREATE TABLE "messages" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "body" character varying(1000), "addons" json, "isRead" boolean NOT NULL DEFAULT false, "fromId" uuid, "toId" uuid, "chatId" uuid, "parentId" uuid, CONSTRAINT "PK_18325f38ae6de43878487eff986" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `ALTER TABLE "users" ADD "status" character varying(150)`,
    );
    await queryRunner.query(
      `ALTER TABLE "chats" ADD CONSTRAINT "FK_2b52a7e9372bfb8bce891ff26ce" FOREIGN KEY ("user1Id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "chats" ADD CONSTRAINT "FK_18e342ebd39357c2587f91ba90f" FOREIGN KEY ("user2Id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "contacts" ADD CONSTRAINT "FK_30ef77942fc8c05fcb829dcc61d" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "contacts" ADD CONSTRAINT "FK_2f2eeb268dcaf6e7f1c2176949f" FOREIGN KEY ("contactId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" ADD CONSTRAINT "FK_627bdb88ff88b446023474e4261" FOREIGN KEY ("fromId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" ADD CONSTRAINT "FK_4d8b2643c29b31e55b13b9213ab" FOREIGN KEY ("toId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" ADD CONSTRAINT "FK_36bc604c820bb9adc4c75cd4115" FOREIGN KEY ("chatId") REFERENCES "chats"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" ADD CONSTRAINT "FK_7d473d0de3669832052cac98b98" FOREIGN KEY ("parentId") REFERENCES "messages"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `CREATE TABLE "message_ratings" ("id" uuid NOT NULL DEFAULT uuid_generate_v4(), "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), "face" character varying(40) NOT NULL, "userId" uuid, "messageId" uuid, CONSTRAINT "PK_c0871419e09e75839627d8539df" PRIMARY KEY ("id"))`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" ADD "edited" boolean NOT NULL DEFAULT false`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" ADD "forwardedFromId" uuid`,
    );
    await queryRunner.query(
      `ALTER TABLE "message_ratings" ADD CONSTRAINT "FK_530fb27455fde5b2b7ba540c53f" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "message_ratings" ADD CONSTRAINT "FK_2f05066ee4a910a0e1810024c17" FOREIGN KEY ("messageId") REFERENCES "messages"("id") ON DELETE CASCADE ON UPDATE NO ACTION`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" ADD CONSTRAINT "FK_5800647d2d98e8a029f141ee925" FOREIGN KEY ("forwardedFromId") REFERENCES "users"("id") ON DELETE SET NULL ON UPDATE NO ACTION`,
    );
  }

  public async down(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.query(
      `ALTER TABLE "notifications" DROP CONSTRAINT "FK_692a909ee0fa9383e7859f9b406"`,
    );
    await queryRunner.query(
      `ALTER TABLE "galleries" DROP CONSTRAINT "FK_e9d0c45df9bf38474ed6cbdd291"`,
    );
    await queryRunner.query(
      `ALTER TABLE "galleries" DROP CONSTRAINT "FK_797bb7d4e10c84afcfadaca1d18"`,
    );
    await queryRunner.query(
      `ALTER TABLE "favorites" DROP CONSTRAINT "FK_307c40c36b91a85aea3f22c20d6"`,
    );
    await queryRunner.query(
      `ALTER TABLE "favorites" DROP CONSTRAINT "FK_e747534006c6e3c2f09939da60f"`,
    );
    await queryRunner.query(
      `ALTER TABLE "bookings" DROP CONSTRAINT "FK_64de318a01c502530b1e32692fd"`,
    );
    await queryRunner.query(
      `ALTER TABLE "bookings" DROP CONSTRAINT "FK_38a69a58a323647f2e75eb994de"`,
    );
    await queryRunner.query(
      `ALTER TABLE "bookings" DROP CONSTRAINT "FK_15a2431ec10d29dcd96c9563b65"`,
    );
    await queryRunner.query(
      `ALTER TABLE "branches" DROP CONSTRAINT "FK_1d08996d9003bfdc86cefe01dee"`,
    );
    await queryRunner.query(
      `ALTER TABLE "branch_users" DROP CONSTRAINT "FK_e5d6aa8cf1ebfdcc242580160cb"`,
    );
    await queryRunner.query(
      `ALTER TABLE "branch_users" DROP CONSTRAINT "FK_c0d1e91764bfdafe504ed8b3717"`,
    );
    await queryRunner.query(
      `ALTER TABLE "services" DROP CONSTRAINT "FK_034b52310c2d211bc979c3cc4e8"`,
    );
    await queryRunner.query(
      `ALTER TABLE "services" DROP CONSTRAINT "FK_3905389899d96c4f1b3619f68d5"`,
    );
    await queryRunner.query(
      `ALTER TABLE "users" DROP CONSTRAINT "FK_368e146b785b574f42ae9e53d5e"`,
    );
    await queryRunner.query(`DROP TABLE "notifications"`);
    await queryRunner.query(`DROP TYPE "public"."notifications_type_enum"`);
    await queryRunner.query(`DROP TABLE "galleries"`);
    await queryRunner.query(`DROP TABLE "favorites"`);
    await queryRunner.query(`DROP TABLE "bookings"`);
    await queryRunner.query(`DROP TYPE "public"."bookings_status_enum"`);
    await queryRunner.query(`DROP TABLE "branches"`);
    await queryRunner.query(`DROP TYPE "public"."branches_unit_enum"`);
    await queryRunner.query(`DROP TABLE "branch_users"`);
    await queryRunner.query(`DROP TABLE "services"`);
    await queryRunner.query(`DROP TYPE "public"."services_unit_enum"`);
    await queryRunner.query(`DROP TABLE "categories"`);
    await queryRunner.query(`DROP TYPE "public"."categories_type_enum"`);
    await queryRunner.query(`DROP TABLE "users"`);
    await queryRunner.query(`DROP TYPE "public"."users_locale_enum"`);
    await queryRunner.query(`DROP TABLE "roles"`);
    await queryRunner.query(
      `ALTER TABLE "branch_ratings" DROP CONSTRAINT "FK_eac2bfe2665ab444c4b2e3edd55"`,
    );
    await queryRunner.query(
      `ALTER TABLE "branch_ratings" DROP CONSTRAINT "FK_5e2f9fa771c014e0f412d7ae28d"`,
    );
    await queryRunner.query(
      `ALTER TABLE "service_ratings" DROP CONSTRAINT "FK_58849e8c0ea7fd3f8892836c1d7"`,
    );
    await queryRunner.query(
      `ALTER TABLE "service_ratings" DROP CONSTRAINT "FK_5767e6dfeeaa708984a33426fc4"`,
    );
    await queryRunner.query(`ALTER TABLE "branches" ADD "rating" integer`);
    await queryRunner.query(`ALTER TABLE "services" ADD "rating" integer`);
    await queryRunner.query(`DROP TABLE "branch_ratings"`);
    await queryRunner.query(`DROP TABLE "service_ratings"`);
    await queryRunner.query(
      `CREATE TYPE "public"."notifications_type_enum_old" AS ENUM('NEW_BOOKING', 'BOOKING_CANCELED', 'USER_ADDED_TO_BRANCH', 'USER_REMOVED_FROM_BRANCH', 'REBOOKED', 'FEE_CHARGED', 'PAYMENT_PERIOD_APPROACHES', 'SERVICE_BLOCKED_DUE_TO_LACK_BALANCE', 'TRIAL_PERIOD_END_APPROACHES', 'BOOKING_FINISHED')`,
    );
    await queryRunner.query(
      `ALTER TABLE "notifications" ALTER COLUMN "type" TYPE "public"."notifications_type_enum_old" USING "type"::"text"::"public"."notifications_type_enum_old"`,
    );
    await queryRunner.query(`DROP TYPE "public"."notifications_type_enum"`);
    await queryRunner.query(
      `ALTER TYPE "public"."notifications_type_enum_old" RENAME TO "notifications_type_enum"`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" DROP CONSTRAINT "FK_7d473d0de3669832052cac98b98"`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" DROP CONSTRAINT "FK_36bc604c820bb9adc4c75cd4115"`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" DROP CONSTRAINT "FK_4d8b2643c29b31e55b13b9213ab"`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" DROP CONSTRAINT "FK_627bdb88ff88b446023474e4261"`,
    );
    await queryRunner.query(
      `ALTER TABLE "contacts" DROP CONSTRAINT "FK_2f2eeb268dcaf6e7f1c2176949f"`,
    );
    await queryRunner.query(
      `ALTER TABLE "contacts" DROP CONSTRAINT "FK_30ef77942fc8c05fcb829dcc61d"`,
    );
    await queryRunner.query(
      `ALTER TABLE "chats" DROP CONSTRAINT "FK_18e342ebd39357c2587f91ba90f"`,
    );
    await queryRunner.query(
      `ALTER TABLE "chats" DROP CONSTRAINT "FK_2b52a7e9372bfb8bce891ff26ce"`,
    );
    await queryRunner.query(`ALTER TABLE "users" DROP COLUMN "status"`);
    await queryRunner.query(`DROP TABLE "messages"`);
    await queryRunner.query(`DROP TABLE "contacts"`);
    await queryRunner.query(`DROP TABLE "chats"`);
    await queryRunner.query(
      `ALTER TABLE "messages" DROP CONSTRAINT "FK_5800647d2d98e8a029f141ee925"`,
    );
    await queryRunner.query(
      `ALTER TABLE "message_ratings" DROP CONSTRAINT "FK_2f05066ee4a910a0e1810024c17"`,
    );
    await queryRunner.query(
      `ALTER TABLE "message_ratings" DROP CONSTRAINT "FK_530fb27455fde5b2b7ba540c53f"`,
    );
    await queryRunner.query(
      `ALTER TABLE "messages" DROP COLUMN "forwardedFromId"`,
    );
    await queryRunner.query(`ALTER TABLE "messages" DROP COLUMN "edited"`);
    await queryRunner.query(`DROP TABLE "message_ratings"`);
  }
}
