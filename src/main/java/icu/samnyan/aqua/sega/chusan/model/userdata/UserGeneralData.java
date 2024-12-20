package icu.samnyan.aqua.sega.chusan.model.userdata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is for storing the other data that doesn't need to save it in a separate table
 * @author samnyan (privateamusement@protonmail.com)
 */
@Entity(name = "ChusanUserGeneralData")
@Table(name = "chusan_user_general_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGeneralData extends Chu3UserEntity {
    private String propertyKey;

    @Column(columnDefinition = "TEXT")
    private String propertyValue;

    public UserGeneralData(Chu3UserData userData, String key) {
        setUser(userData);
        this.propertyKey = key;
        this.propertyValue = "";
    }
}
