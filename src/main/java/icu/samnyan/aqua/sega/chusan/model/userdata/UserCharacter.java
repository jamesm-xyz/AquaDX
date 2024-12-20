package icu.samnyan.aqua.sega.chusan.model.userdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@Entity(name = "ChusanUserCharacter")
@Table(name = "chusan_user_character", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "character_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"characterId", "playCount", "level", "friendshipExp", "isValid", "isNewMark", "exMaxLv", "assignIllust", "param1", "param2"})
public class UserCharacter extends Chu3UserEntity {
    @Column(name = "character_id")
    private int characterId;

    private int playCount = 0;

    private int level = 1;

    private int friendshipExp = 0;

    @JsonProperty("isValid")
    private boolean isValid = true;

    @JsonProperty("isNewMark")
    private boolean isNewMark = true;

    private int exMaxLv = 0;

    private int assignIllust = 0;

    private int param1 = 0;

    private int param2 = 0;

    public UserCharacter(Chu3UserData userData) {
        setUser(userData);
    }
}
