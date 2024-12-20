package icu.samnyan.aqua.sega.chusan.model.userdata;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Entity(name = "ChusanUserCardPrintState")
@Table(name = "chusan_user_print_state")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCardPrintState extends Chu3UserEntity {
    private boolean hasCompleted;
    private LocalDateTime limitDate;
    private int placeId;
    private int cardId;
    private int gachaId;
}
