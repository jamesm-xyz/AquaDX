package icu.samnyan.aqua.sega.chusan.model.userdata

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity(name = "ChusanUserCMission")
@Table(name = "chusan_user_cmission")
class UserCMission : Chu3UserEntity() {
    @Column(name = "mission_id")
    var missionId = 0

    @Column(name = "point")
    var point = 0
}
