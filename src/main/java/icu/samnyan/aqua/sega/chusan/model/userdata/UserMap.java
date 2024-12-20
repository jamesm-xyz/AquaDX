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
@Entity(name = "ChusanUserMapArea")
@Table(name = "chusan_user_map_area", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "map_area_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "mapAreaId",
        "position",
        "isClear",
        "rate",
        "statusCount",
        "remainGridCount",
        "isLocked"
})
public class UserMap extends Chu3UserEntity {
    @Column(name = "map_area_id")
    private int mapAreaId;

    private int position;

    @JsonProperty("isClear")
    private boolean isClear;

    private int rate;

    private int statusCount;

    private int remainGridCount;

    @JsonProperty("isLocked")
    private boolean isLocked;

    public UserMap(Chu3UserData userData) {
        setUser(userData);
    }
}
