import { DataSource, DataSourceOptions } from "typeorm";
import * as dotenv from 'dotenv';
import * as path from 'path';
import { entities } from "@/entities";
import { migrations } from "@/migrations";

dotenv.config({ path: path.join(process.cwd(), '.env') });

export const dataSourceOptions: DataSourceOptions = {
    type: 'postgres',
    username: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    host: process.env.DB_HOST,
    port: Number(process.env.DB_PORT),
    synchronize: false,
    entities,
    migrations,
    database: process.env.DB_NAME,
};

const dataSource = new DataSource(dataSourceOptions);

export default dataSource;