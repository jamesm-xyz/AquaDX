package icu.samnyan.aqua.sega.chusan.service;

import icu.samnyan.aqua.sega.chusan.model.Chu3UserActivityRepo;
import icu.samnyan.aqua.sega.chusan.model.userdata.UserActivity;
import icu.samnyan.aqua.sega.chusan.model.userdata.Chu3UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Service("ChusanUserActivityService")
public class UserActivityService {

    private final Chu3UserActivityRepo userActivityRepository;

    @Autowired
    public UserActivityService(Chu3UserActivityRepo userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }

    public List<UserActivity> getByUserId(String userId) {
        return userActivityRepository.findByUser_Card_ExtId(Long.parseLong(userId));
    }
}
