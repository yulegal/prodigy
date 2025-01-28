import { Column, Entity, JoinTable, ManyToMany, ManyToOne } from "typeorm";
import { BaseEntity } from "./BaseEntity";
import { UserEnitity } from "./UserEntity";
import { PostVisibility } from '@shared/interfaces/post';
import { TagEntity } from "./TagEntity";

@Entity('posts')
export class PostEntity extends BaseEntity {
    @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
    user: UserEnitity;

    @Column({ length: 3000, nullable: true })
    body: string;

    @Column({ type: 'json', nullable: true })
    addons: string[];

    @Column({ type: 'enum', enum: PostVisibility })
    visibility: PostVisibility;

    @ManyToMany(() => TagEntity)
    @JoinTable()
    tags: TagEntity[];
}