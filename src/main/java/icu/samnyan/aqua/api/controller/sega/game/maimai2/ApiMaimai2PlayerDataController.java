package icu.samnyan.aqua.api.controller.sega.game.maimai2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import icu.samnyan.aqua.api.model.MessageResponse;
import icu.samnyan.aqua.api.model.ReducedPageResponse;
import icu.samnyan.aqua.api.model.resp.sega.maimai2.PhotoResp;
import icu.samnyan.aqua.api.model.resp.sega.maimai2.ProfileResp;
import icu.samnyan.aqua.api.model.resp.sega.maimai2.external.ExternalUserData;
import icu.samnyan.aqua.api.model.resp.sega.maimai2.external.Maimai2DataExport;
import icu.samnyan.aqua.api.model.resp.sega.maimai2.external.Maimai2DataImport;
import icu.samnyan.aqua.api.util.ApiMapper;
import icu.samnyan.aqua.sega.general.model.Card;
import icu.samnyan.aqua.sega.general.service.CardService;
import icu.samnyan.aqua.sega.maimai2.model.*;
import icu.samnyan.aqua.sega.maimai2.model.userdata.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@RestController
@RequestMapping("api/game/maimai2")
@AllArgsConstructor
public class ApiMaimai2PlayerDataController {

    private final ApiMapper mapper;

    private final CardService cardService;

    private final Mai2UserActRepo userActRepository;
    private final Mai2UserCharacterRepo userCharacterRepository;
    private final Mai2UserDataRepo userDataRepository;
    private final Mai2UserItemRepo userItemRepository;
    private final Mai2UserLoginBonusRepo userLoginBonusRepository;
    private final Mai2UserMusicDetailRepo userMusicDetailRepository;
    private final Mai2UserOptionRepo userOptionRepository;
    private final Mai2UserPlaylogRepo userPlaylogRepository;
    private final Mai2UserGeneralDataRepo userGeneralDataRepository;
    private final Mai2MapEncountNpcRepo mapEncountNpcRepository;
    private final Mai2UserChargeRepo userChargeRepository;
    private final Mai2UserCourseRepo userCourseRepository;
    private final Mai2UserExtendRepo userExtendRepository;
    private final Mai2UserFavoriteRepo userFavoriteRepository;
    private final Mai2UserFriendSeasonRankingRepo userFriendSeasonRankingRepository;
    private final Mai2UserMapRepo userMapRepository;
    private final Mai2UserUdemaeRepo userUdemaeRepository;

    @GetMapping("config/userPhoto/divMaxLength")
    public long getConfigUserPhotoDivMaxLength(@Value("${game.maimai2.userPhoto.divMaxLength:32}") long divMaxLength) {
        return divMaxLength;
    }

    @GetMapping("userPhoto")
    public PhotoResp getUserPhoto(@RequestParam long aimeId,
                                  @RequestParam(required = false, defaultValue = "0") int imageIndex) {
        List<String> matchedFiles = new ArrayList<>();
        PhotoResp Photo = new PhotoResp();
        try (Stream<Path> paths = Files.walk(Paths.get("data"))) {
            matchedFiles = paths
            .filter(Files::isRegularFile)
            .filter(path -> path.getFileName().toString().endsWith(".jpg"))
            .filter(path -> {
                String fileName = path.getFileName().toString();
                String[] parts = fileName.split("-");
                return parts.length > 0 && parts[0].equals(String.valueOf(aimeId));
            })
            .map(Path::getFileName)
            .map(Path::toString)
            .sorted(Comparator.reverseOrder())
            .toList();
            Photo.setTotalImage(matchedFiles.size());
            Photo.setImageIndex(imageIndex);
            if(matchedFiles.size() > imageIndex) {
                byte[] targetImageContent = Files.readAllBytes(Paths.get("data/" + matchedFiles.get(imageIndex)));
                String divData = Base64.getEncoder().encodeToString(targetImageContent);
                Photo.setDivData(divData);
                Photo.setFileName(matchedFiles.get(imageIndex));
            }
        }
        catch (Exception e) {
        }
        return Photo;
    }

    @GetMapping("profile")
    public ProfileResp getProfile(@RequestParam long aimeId) {
        return mapper.convert(userDataRepository.findByCardExtId(aimeId).orElseThrow(), new TypeReference<>() {
        });
    }

    @PostMapping("profile/username")
    public Mai2UserDetail updateName(@RequestBody Map<String, Object> request) {
        Mai2UserDetail profile = userDataRepository.findByCardExtId(((Number) request.get("aimeId")).longValue()).orElseThrow();
        profile.setUserName((String) request.get("userName"));
        return userDataRepository.save(profile);
    }

    @PostMapping("profile/icon")
    public Mai2UserDetail updateIcon(@RequestBody Map<String, Object> request) {
        Mai2UserDetail profile = userDataRepository.findByCardExtId(((Number) request.get("aimeId")).longValue()).orElseThrow();
        profile.setIconId((Integer) request.get("iconId"));
        return userDataRepository.save(profile);
    }

    @PostMapping("profile/plate")
    public Mai2UserDetail updatePlate(@RequestBody Map<String, Object> request) {
        Mai2UserDetail profile = userDataRepository.findByCardExtId(((Number) request.get("aimeId")).longValue()).orElseThrow();
        profile.setPlateId((Integer) request.get("plateId"));
        return userDataRepository.save(profile);
    }

    @PostMapping("profile/frame")
    public Mai2UserDetail updateFrame(@RequestBody Map<String, Object> request) {
        Mai2UserDetail profile = userDataRepository.findByCardExtId(((Number) request.get("aimeId")).longValue()).orElseThrow();
        profile.setFrameId((Integer) request.get("frameId"));
        return userDataRepository.save(profile);
    }

    @PostMapping("profile/title")
    public Mai2UserDetail updateTrophy(@RequestBody Map<String, Object> request) {
        Mai2UserDetail profile = userDataRepository.findByCardExtId(((Number) request.get("aimeId")).longValue()).orElseThrow();
        profile.setTitleId((Integer) request.get("titleId"));
        return userDataRepository.save(profile);
    }

    @PostMapping("profile/partner")
    public Mai2UserDetail updatePartner(@RequestBody Map<String, Object> request) {
        Mai2UserDetail profile = userDataRepository.findByCardExtId(((Number) request.get("aimeId")).longValue()).orElseThrow();
        profile.setPartnerId((Integer) request.get("partnerId"));
        return userDataRepository.save(profile);
    }

    @GetMapping("character")
    public ReducedPageResponse<Mai2UserCharacter> getCharacter(@RequestParam long aimeId,
                                                               @RequestParam(required = false, defaultValue = "0") int page,
                                                               @RequestParam(required = false, defaultValue = "10") int size) {
        Page<Mai2UserCharacter> characters = userCharacterRepository.findByUser_Card_ExtId(aimeId, PageRequest.of(page, size));
        return new ReducedPageResponse<>(characters.getContent(), characters.getPageable().getPageNumber(), characters.getTotalPages(), characters.getTotalElements());
    }

    @GetMapping("activity")
    public List<Mai2UserAct> getActivities(@RequestParam long aimeId) {
        return userActRepository.findByUser_Card_ExtId(aimeId);
    }

    @GetMapping("item")
    public ReducedPageResponse<Mai2UserItem> getItem(@RequestParam long aimeId,
                                                     @RequestParam(required = false, defaultValue = "0") int page,
                                                     @RequestParam(required = false, defaultValue = "10") int size,
                                                     @RequestParam(required = false, defaultValue = "0") int ItemKind) {
        Page<Mai2UserItem> items;
        if(ItemKind == 0){
            items = userItemRepository.findByUser_Card_ExtId(aimeId, PageRequest.of(page, size));
        }
        else{
            items = userItemRepository.findByUser_Card_ExtIdAndItemKind(aimeId, ItemKind, PageRequest.of(page, size));
        }
        return new ReducedPageResponse<>(items.getContent(), items.getPageable().getPageNumber(), items.getTotalPages(), items.getTotalElements());
    }

    @PostMapping("item")
    public ResponseEntity<Object> updateItem(@RequestBody Map<String, Object> request) {
        Mai2UserDetail profile = userDataRepository.findByCardExtId(((Number) request.get("aimeId")).longValue()).orElseThrow();
        Integer itemKind = (Integer) request.get("itemKind");
        Integer itemId = (Integer) request.get("itemId");
        int stock = 1;
        if (request.containsKey("stock")) {
            stock = (Integer) request.get("stock");
        }

        Optional<Mai2UserItem> userItemOptional = userItemRepository.findByUserAndItemKindAndItemId(profile, itemKind, itemId);

        Mai2UserItem userItem;
        if (userItemOptional.isPresent()) {
            userItem = userItemOptional.get();
        } else {
            userItem = new Mai2UserItem();
            userItem.setUser(profile);
            userItem.setItemId(itemId);
            userItem.setItemKind(itemKind);
        }
        userItem.setStock(stock);
        userItem.setValid(true);
        return ResponseEntity.ok(userItemRepository.save(userItem));
    }

    @GetMapping("recent")
    public ReducedPageResponse<Mai2UserPlaylog> getRecent(@RequestParam long aimeId,
                                                          @RequestParam(required = false, defaultValue = "0") int page,
                                                          @RequestParam(required = false, defaultValue = "10") int size) {
        Page<Mai2UserPlaylog> playlogs = userPlaylogRepository.findByUser_Card_ExtId(aimeId, PageRequest.of(page, size, Sort.Direction.DESC, "id"));
        return new ReducedPageResponse<>(playlogs.getContent(), playlogs.getPageable().getPageNumber(), playlogs.getTotalPages(), playlogs.getTotalElements());

    }

    @GetMapping("song/{id}")
    public List<Mai2UserMusicDetail> getSongDetail(@RequestParam long aimeId, @PathVariable int id) {
        return userMusicDetailRepository.findByUser_Card_ExtIdAndMusicId(aimeId, id);
    }

    @GetMapping("song/{id}/{level}")
    public List<Mai2UserPlaylog> getLevelPlaylog(@RequestParam long aimeId, @PathVariable int id, @PathVariable int level) {
        return userPlaylogRepository.findByUser_Card_ExtIdAndMusicIdAndLevel(aimeId, id, level);
    }

    @GetMapping("options")
    public Mai2UserOption getOptions(@RequestParam long aimeId) {
        return userOptionRepository.findSingleByUser_Card_ExtId(aimeId).orElseThrow();
    }

    @PostMapping("options")
    public ResponseEntity<Object> updateOptions(@RequestBody Map<String, Object> request) {
		Mai2UserDetail profile = userDataRepository.findByCardExtId(((Number) request.get("aimeId")).longValue()).orElseThrow();
		ObjectMapper objectMapper = new ObjectMapper();
		Mai2UserOption userOption = objectMapper.convertValue(request.get("options"), Mai2UserOption.class);
		userOption.setUser(profile);
		userOptionRepository.deleteByUser(profile);
		userOptionRepository.flush();
		return ResponseEntity.ok(userOptionRepository.save(userOption));
    }

    @GetMapping("general")
    public ResponseEntity<Object> getGeneralData(@RequestParam long aimeId, @RequestParam String key) {
        Optional<Mai2UserGeneralData> userGeneralDataOptional = userGeneralDataRepository.findByUser_Card_ExtIdAndPropertyKey(aimeId, key);
        return userGeneralDataOptional.<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User or value not found.")));
    }

    @PostMapping("general")
    public ResponseEntity<Object> setGeneralData(@RequestBody Map<String, Object> request) {
        Mai2UserDetail profile = userDataRepository.findByCardExtId(((Number) request.get("aimeId")).longValue()).orElseThrow();
        String key = (String) request.get("key");
        String value = (String) request.get("value");

        Optional<Mai2UserGeneralData> userGeneralDataOptional = userGeneralDataRepository.findByUserAndPropertyKey(profile, key);
        Mai2UserGeneralData userGeneralData;
        if (userGeneralDataOptional.isPresent()) {
            userGeneralData = userGeneralDataOptional.get();
        }
        else {
            userGeneralData = new Mai2UserGeneralData();
            userGeneralData.setUser(profile);
            userGeneralData.setPropertyKey(key);
        }
        userGeneralData.setPropertyValue(value);

        return ResponseEntity.ok(userGeneralDataRepository.save(userGeneralData));
    }

    @GetMapping("export")
    public ResponseEntity<Object> exportAllUserData(@RequestParam long aimeId) {
        Maimai2DataExport data = new Maimai2DataExport();
        try {
            data.setGameId("SDEZ");
            data.setUserData(userDataRepository.findByCardExtId(aimeId).orElseThrow());
            data.setUserExtend(userExtendRepository.findSingleByUser_Card_ExtId(aimeId).orElseThrow());
            data.setUserOption(userOptionRepository.findSingleByUser_Card_ExtId(aimeId).orElseThrow());
            data.setUserUdemae(userUdemaeRepository.findSingleByUser_Card_ExtId(aimeId).orElseThrow());
            data.setUserCharacterList(userCharacterRepository.findByUser_Card_ExtId(aimeId));
            data.setUserGeneralDataList(userGeneralDataRepository.findByUser_Card_ExtId(aimeId));
            data.setUserItemList(userItemRepository.findByUser_Card_ExtId(aimeId));
            data.setUserLoginBonusList(userLoginBonusRepository.findByUser_Card_ExtId(aimeId));
            data.setUserMusicDetailList(userMusicDetailRepository.findByUser_Card_ExtId(aimeId));
            data.setUserPlaylogList(userPlaylogRepository.findByUserCardExtId(aimeId));
            data.setMapEncountNpcList(mapEncountNpcRepository.findByUser_Card_ExtId(aimeId));
            data.setUserActList(userActRepository.findByUser_Card_ExtId(aimeId));
            data.setUserChargeList(userChargeRepository.findByUser_Card_ExtId(aimeId));
            data.setUserCourseList(userCourseRepository.findByUser_Card_ExtId(aimeId));
            data.setUserFavoriteList(userFavoriteRepository.findByUser_Card_ExtId(aimeId));
            data.setUserFriendSeasonRankingList(userFriendSeasonRankingRepository.findByUser_Card_ExtId(aimeId));
            data.setUserMapList(userMapRepository.findByUser_Card_ExtId(aimeId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error during data export. Reason: " + e.getMessage()));
        }
        // Set filename
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-disposition", "attachment; filename=maimai2_" + aimeId + "_exported.json");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("import")
    public ResponseEntity<Object> importAllUserData(@RequestBody Maimai2DataImport data) {
        if (!data.getGameId().equals("SDEZ")) {
            return ResponseEntity.unprocessableEntity().body(new MessageResponse("Wrong Game Profile, Expected 'SDEZ', Get " + data.getGameId()));
        }

        ExternalUserData exUser = data.getUserData();

        Optional<Card> cardOptional = cardService.getCardByAccessCode(exUser.getAccessCode());
        Card card;
        if (cardOptional.isPresent()) {
            card = cardOptional.get();
            Optional<Mai2UserDetail> existUserData = Optional.ofNullable(userDataRepository.findByCard(cardOptional.get()));
            if (existUserData.isPresent()) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(new MessageResponse("This card already has a maimai2 profile."));
                // delete all same card data
                userFavoriteRepository.deleteByUser(existUserData.get());
                userFavoriteRepository.flush();
                userFriendSeasonRankingRepository.deleteByUser(existUserData.get());
                userFriendSeasonRankingRepository.flush();
                userMapRepository.deleteByUser(existUserData.get());
                userMapRepository.flush();
                userUdemaeRepository.deleteByUser(existUserData.get());
                userUdemaeRepository.flush();
                userGeneralDataRepository.deleteByUser(existUserData.get());
                userGeneralDataRepository.flush();
                userItemRepository.deleteByUser(existUserData.get());
                userItemRepository.flush();
                userLoginBonusRepository.deleteByUser(existUserData.get());
                userLoginBonusRepository.flush();
                userMusicDetailRepository.deleteByUser(existUserData.get());
                userMusicDetailRepository.flush();
                userOptionRepository.deleteByUser(existUserData.get());
                userOptionRepository.flush();
                userPlaylogRepository.deleteByUser(existUserData.get());
                userPlaylogRepository.flush();
                userCharacterRepository.deleteByUser(existUserData.get());
                userCharacterRepository.flush();
                mapEncountNpcRepository.deleteByUser(existUserData.get());
                mapEncountNpcRepository.flush();
                userActRepository.deleteByUser(existUserData.get());
                userActRepository.flush();
                userChargeRepository.deleteByUser(existUserData.get());
                userChargeRepository.flush();
                userCourseRepository.deleteByUser(existUserData.get());
                userCourseRepository.flush();
                userExtendRepository.deleteByUser(existUserData.get());
                userExtendRepository.flush();
                userOptionRepository.deleteByUser(existUserData.get());
                userOptionRepository.flush();

                userDataRepository.deleteByCard(card);
                userDataRepository.flush();
            }
        } else {
            card = cardService.registerByAccessCode(exUser.getAccessCode());
        }

        Mai2UserDetail userData = mapper.convert(exUser, new TypeReference<>() {
        });
        userData.setCard(card);
        userDataRepository.saveAndFlush(userData);

        userFavoriteRepository.saveAll(data.getUserFavoriteList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userFriendSeasonRankingRepository.saveAll(data.getUserFriendSeasonRankingList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userMapRepository.saveAll(data.getUserMapList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userGeneralDataRepository.saveAll(data.getUserGeneralDataList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userItemRepository.saveAll(data.getUserItemList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userLoginBonusRepository.saveAll(data.getUserLoginBonusList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userMusicDetailRepository.saveAll(data.getUserMusicDetailList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userPlaylogRepository.saveAll(data.getUserPlaylogList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userCharacterRepository.saveAll(data.getUserCharacterList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        mapEncountNpcRepository.saveAll(data.getMapEncountNpcList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userActRepository.saveAll(data.getUserActList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userChargeRepository.saveAll(data.getUserChargeList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));
        userCourseRepository.saveAll(data.getUserCourseList().stream().peek(x -> x.setUser(userData)).collect(Collectors.toList()));

        Mai2UserExtend userExtend = data.getUserExtend();
        userExtend.setUser(userData);
        userExtendRepository.save(userExtend);

        Mai2UserOption userOption = data.getUserOption();
        userOption.setUser(userData);
        userOptionRepository.save(userOption);

        Mai2UserUdemae userUdemae = data.getUserUdemae();
        userUdemae.setUser(userData);
        userUdemaeRepository.save(userUdemae);

        return ResponseEntity.ok(new MessageResponse("Import successfully, aimeId: " + card.getExtId()));
    }

}
