package icu.samnyan.aqua.sega.chusan.model.userdata;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@Entity(name = "ChusanUserCharge")
@Table(name = "chusan_user_charge", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "charge_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"chargeId", "stock", "purchaseDate", "validDate", "param1", "param2", "paramDate"})
public class UserCharge extends Chu3UserEntity {
    @Column(name = "charge_id")
    private int chargeId;

    private int stock;

    private LocalDateTime purchaseDate;

    private LocalDateTime validDate;

    private int param1;

    private int param2;

    private LocalDateTime paramDate;

    public UserCharge(Chu3UserData user) {
        setUser(user);
    }
}
