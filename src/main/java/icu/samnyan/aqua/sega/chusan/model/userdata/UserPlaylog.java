package icu.samnyan.aqua.sega.chusan.model.userdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import icu.samnyan.aqua.net.games.IGenericGamePlaylog;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Entity(name = "ChusanUserPlaylog")
@Table(name = "chusan_user_playlog")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPlaylog implements Serializable, IGenericGamePlaylog {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Chu3UserData user;

    private String romVersion;

    private int orderId;

    private int sortNumber;

    private int placeId;

    private LocalDateTime playDate;

    private LocalDateTime userPlayDate;

    private int musicId;

    private int level;

    private int customId;

    private long playedUserId1;

    private long playedUserId2;

    private long playedUserId3;

    private String playedUserName1;

    private String playedUserName2;

    private String playedUserName3;

    private int playedMusicLevel1;

    private int playedMusicLevel2;

    private int playedMusicLevel3;

    private int playedCustom1;

    private int playedCustom2;

    private int playedCustom3;

    private int track;

    private int score;

    @Column(name = "\"rank\"")
    private int rank;

    private int maxCombo;

    private int maxChain;

    private int rateTap;

    private int rateHold;

    private int rateSlide;

    private int rateAir;

    private int rateFlick;

    private int judgeGuilty;

    private int judgeAttack;

    private int judgeJustice;

    private int judgeCritical;

    private int judgeHeaven;

    private int eventId;

    private int playerRating;

    @JsonProperty("isNewRecord")
    private boolean isNewRecord;

    @JsonProperty("isFullCombo")
    private boolean isFullCombo;

    private int fullChainKind;

    @JsonProperty("isAllJustice")
    private boolean isAllJustice;

    @JsonProperty("isContinue")
    private boolean isContinue;

    @JsonProperty("isFreeToPlay")
    private boolean isFreeToPlay;

    private int characterId;

    private int charaIllustId;

    private int skillId;

    private int playKind;

    @JsonProperty("isClear")
    private boolean isClear;

    private int skillLevel;

    private int skillEffect;

    private String placeName;

    private int commonId;

    // SUN
    private int regionId;

    private int machineType;

    // SUN PLUS
    private int ticketId;

    public UserPlaylog(Chu3UserData userData) {
        user = userData;
    }

    @Override
    public int getAchievement() {
        return score;
    }

    @Override
    public int getAfterRating() {
        return playerRating;
    }

    @Override
    public int getBeforeRating() {
        return playerRating; // TODO: Get before rating
    }

    @Override
    public boolean isAllPerfect() {
        return isAllJustice;
    }
}
