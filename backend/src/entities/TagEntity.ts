import { Column, Entity } from "typeorm";
import { BaseEntity } from "./BaseEntity";

@Entity('tags')
export class TagEntity extends BaseEntity {
    @Column({ length: 45 })
    name: string;
}