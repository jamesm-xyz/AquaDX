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
@Entity(name = "ChusanUserActivity")
@Table(name = "chusan_user_activity", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "kind", "activity_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"kind", "id", "sortNumber", "param1", "param2", "param3", "param4"})
public class UserActivity extends Chu3UserEntity {
    private int kind;

    @JsonProperty("id")
    @Column(name = "activity_id")
    private int activityId;

    private int sortNumber;

    private int param1;

    private int param2;

    private int param3;

    private int param4;

    public UserActivity(Chu3UserData userData) { setUser(userData); }
}
