package icu.samnyan.aqua.sega.chusan.model.userdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Entity(name = "ChusanUserCourse")
@Table(name = "chusan_user_course", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "course_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCourse extends Chu3UserEntity {
    @Column(name = "course_id")
    private int courseId;

    private int classId;

    private int playCount;

    private int theoryCount;

    private int scoreMax;

    @JsonProperty("isFullCombo")
    private boolean isFullCombo;

    @JsonProperty("isAllJustice")
    private boolean isAllJustice;

    @JsonProperty("isSuccess")
    private boolean isSuccess;

    private int scoreRank;

    private int eventId;

    private LocalDateTime lastPlayDate;

    private int param1;

    private int param2;

    private int param3;

    private int param4;

    private int orderId;

    private int playerRating;

    @JsonProperty("isClear")
    private boolean isClear;

    public UserCourse(Chu3UserData userData) {
        setUser(userData);
    }

    public UserCourse(int classId) {
        this.classId = classId;
    }
}
