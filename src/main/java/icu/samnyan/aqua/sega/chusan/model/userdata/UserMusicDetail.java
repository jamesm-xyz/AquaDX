package icu.samnyan.aqua.sega.chusan.model.userdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import icu.samnyan.aqua.sega.chusan.util.BooleanToIntegerDeserializer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Entity(name = "ChusanUserMusicDetail")
@Table(name = "chusan_user_music_detail", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "music_id", "level"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "musicId",
        "level",
        "playCount",
        "scoreMax",
        "missCount",
        "maxComboCount",
        "isFullCombo",
        "isAllJustice",
        "isSuccess",
        "fullChain",
        "maxChain",
        "isLock",
        "theoryCount",
        "ext1"
})
public class UserMusicDetail extends Chu3UserEntity {
    @Column(name = "music_id")
    private int musicId;

    private int level;

    private int playCount;

    private int scoreMax;

    private int missCount;

    private int maxComboCount;

    @JsonProperty("isFullCombo")
    private boolean isFullCombo;

    @JsonProperty("isAllJustice")
    private boolean isAllJustice;

    @JsonDeserialize(using = BooleanToIntegerDeserializer.class)
    @JsonProperty("isSuccess")
    private int isSuccess;

    private int fullChain;

    private int maxChain;

    private int scoreRank;

    @JsonProperty("isLock")
    private boolean isLock;

    private int theoryCount;

    private int ext1;

    public UserMusicDetail(Chu3UserData userData) {
        setUser(userData);
    }
}
