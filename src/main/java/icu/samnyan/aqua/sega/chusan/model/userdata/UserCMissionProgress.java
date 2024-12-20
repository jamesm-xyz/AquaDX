package icu.samnyan.aqua.sega.chusan.model.userdata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "ChusanUserCMissionProgress")
@Table(name = "chusan_user_cmission_progress", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "mission_id", "order"})})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCMissionProgress extends Chu3UserEntity {
    @Column(name = "mission_id")
    private int missionId;

    @Column(name = "`order`")
    private int order;

    private int stage;

    private int progress;
}
