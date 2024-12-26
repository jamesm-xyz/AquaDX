package icu.samnyan.aqua.sega.chusan.model.userdata

import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity(name = "ChusanUserCardPrintState")
@Table(name = "chusan_user_print_state")
class UserCardPrintState : Chu3UserEntity() {
    var hasCompleted = false
    var limitDate: LocalDateTime = LocalDateTime.now()
    var placeId = 0
    var cardId = 0
    var gachaId = 0
}
