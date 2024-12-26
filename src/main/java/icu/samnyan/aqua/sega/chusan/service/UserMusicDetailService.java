package icu.samnyan.aqua.sega.chusan.service;

import icu.samnyan.aqua.sega.chusan.model.Chu3UserMusicDetailRepo;
import icu.samnyan.aqua.sega.chusan.model.userdata.Chu3UserData;
import icu.samnyan.aqua.sega.chusan.model.userdata.UserMusicDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Service("ChusanUserMusicDetailService")
public class UserMusicDetailService {

    private final Chu3UserMusicDetailRepo userMusicDetailRepository;

    @Autowired
    public UserMusicDetailService(Chu3UserMusicDetailRepo userMusicDetailRepository) {
        this.userMusicDetailRepository = userMusicDetailRepository;
    }

    public List<UserMusicDetail> getByUserId(String userId) {
        return userMusicDetailRepository.findByUser_Card_ExtId(Long.parseLong(userId));
    }

    public Page<UserMusicDetail> getByUserId(String userId, Pageable page) {
        return userMusicDetailRepository.findByUser_Card_ExtId(Long.parseLong(userId), page);
    }

    public List<UserMusicDetail> getByUserIdAndMusicId(String userId, int musicId) {
        return userMusicDetailRepository.findByUser_Card_ExtIdAndMusicId(Long.parseLong(userId), musicId);
    }
}
