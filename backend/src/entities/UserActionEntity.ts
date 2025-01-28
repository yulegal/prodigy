import { Column, Entity, ManyToOne } from "typeorm";
import { BaseEntity } from "./BaseEntity";
import { UserEnitity } from "./UserEntity";

@Entity('user_actions')
export class UserActionEntity extends BaseEntity {
    @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
    user: UserEnitity;

    @ManyToOne(() => UserEnitity, { onDelete: 'CASCADE' })
    actionUser: UserEnitity;

    @Column({ default: false })
    muted: boolean;

    @Column({ default: false })
    blocked: boolean;
}