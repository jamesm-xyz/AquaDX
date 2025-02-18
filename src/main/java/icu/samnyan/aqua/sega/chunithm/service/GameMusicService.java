package icu.samnyan.aqua.sega.chunithm.service;

import icu.samnyan.aqua.sega.chunithm.dao.gamedata.GameMusicRepository;
import icu.samnyan.aqua.sega.chunithm.model.gamedata.Music;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Service
public class GameMusicService {

    private final GameMusicRepository gameMusicRepository;

    @Autowired
    public GameMusicService(GameMusicRepository gameMusicRepository) {
        this.gameMusicRepository = gameMusicRepository;
    }

    @Cacheable("music")
    public List<Music> getAll() {
        return gameMusicRepository.findAll();
    }

    public Map<Integer, Music> getIdMap() {
        Map<Integer, Music> musicMap = new LinkedHashMap<>();
        getAll().forEach(music -> musicMap.put(music.getMusicId(), music));
        return musicMap;
    }
}
