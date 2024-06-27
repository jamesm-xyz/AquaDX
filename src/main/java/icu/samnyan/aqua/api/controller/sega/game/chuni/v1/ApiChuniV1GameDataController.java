package icu.samnyan.aqua.api.controller.sega.game.chuni.v1;

import icu.samnyan.aqua.sega.chunithm.dao.gamedata.GameCharacterRepository;
import icu.samnyan.aqua.sega.chunithm.dao.gamedata.GameCharacterSkillRepository;
import icu.samnyan.aqua.sega.chunithm.model.gamedata.Character;
import icu.samnyan.aqua.sega.chunithm.model.gamedata.CharacterSkill;
import icu.samnyan.aqua.sega.chunithm.model.gamedata.Music;
import icu.samnyan.aqua.sega.chunithm.service.GameMusicService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@RestController
@RequestMapping("api/game/chuni/v1/data")
@AllArgsConstructor
public class ApiChuniV1GameDataController {

    private final GameMusicService gameMusicService;
    private final GameCharacterRepository gameCharacterRepository;
    private final GameCharacterSkillRepository gameCharacterSkillRepository;

    @GetMapping("music")
    public List<Music> getMusic() {
        return gameMusicService.getAll();
    }

    @GetMapping("character")
    public List<Character> getCharacter() {
        return gameCharacterRepository.findAll();
    }

    @GetMapping("skill")
    public List<CharacterSkill> getSkill() {
        return gameCharacterSkillRepository.findAll();
    }
}
