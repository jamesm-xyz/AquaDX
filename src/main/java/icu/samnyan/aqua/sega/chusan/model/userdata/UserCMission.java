package icu.samnyan.aqua.sega.chusan.model.userdata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "ChusanUserCMission")
@Table(name = "chusan_user_cmission")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCMission extends Chu3UserEntity {
    @Column(name = "mission_id")
    private int missionId;

    @Column(name = "point")
    private int point;
}
