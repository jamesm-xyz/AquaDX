package icu.samnyan.aqua.sega.chusan.model.userdata;

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
@Entity(name = "ChusanUserGacha")
@Table(name = "chusan_user_gacha", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "gacha_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGacha extends Chu3UserEntity {
    @Column(name = "gacha_id")
    private int gachaId;

    private int totalGachaCnt;

    private int ceilingGachaCnt;

    private int dailyGachaCnt;

    private int fiveGachaCnt;

    private int elevenGachaCnt;

    private LocalDateTime dailyGachaDate;
}
