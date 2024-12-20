package icu.samnyan.aqua.sega.chusan.model.userdata;

import icu.samnyan.aqua.net.games.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "ChusanUserLoginBonus")
@Table(name = "chusan_user_login_bonus")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginBonus extends BaseEntity {
    private int version;
    private int user;
    private int presetId;
    private int bonusCount;
    private LocalDateTime lastUpdateDate;
    private boolean isWatched;
    private boolean isFinished;

    public UserLoginBonus(int version, int user, int presetId) {
        this.version = version;
        this.user = user;
        this.presetId = presetId;
        this.bonusCount = 0;
        this.lastUpdateDate = LocalDateTime.parse("2018-01-01T00:00:00");
        this.isWatched = false;
        this.isFinished = false;
    }
}
