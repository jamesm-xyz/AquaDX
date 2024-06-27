@file:Suppress("FunctionName")

package icu.samnyan.aqua.sega.chusan.model

import icu.samnyan.aqua.net.games.GenericPlaylogRepo
import icu.samnyan.aqua.net.games.GenericUserDataRepo
import icu.samnyan.aqua.net.games.IUserRepo
import icu.samnyan.aqua.sega.chusan.model.gamedata.*
import icu.samnyan.aqua.sega.chusan.model.userdata.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Component
import java.util.*


@NoRepositoryBean
interface Chu3UserLinked<T> : IUserRepo<Chu3UserData, T> {
    fun findByUser_Card_ExtId(extId: Long): List<T>
    fun findSingleByUser_Card_ExtId(extId: Long): Optional<T>
    fun findByUser_Card_ExtId(extId: Long, pageable: Pageable): Page<T>
}


// This repo cannot be generalized as UserLinked because the entity stores user as an int
// TODO: Find a way to generalize this
interface Chu3UserLoginBonusRepo : JpaRepository<UserLoginBonus, Long> {
    @Query(
        value = "select * from chusan_user_login_bonus where user = ?1 and version = ?2 and is_finished = ?3 order by last_update_date desc",
        nativeQuery = true
    )
    fun findAllLoginBonus(userId: Int, version: Int, isFinished: Int): List<UserLoginBonus>

    @Query(
        value = "select * from chusan_user_login_bonus where user = ?1 and version = ?2 and preset_id = ?3 limit 1",
        nativeQuery = true
    )
    fun findLoginBonus(userId: Int, version: Int, presetId: Int): Optional<UserLoginBonus>
}

interface Chu3UserActivityRepo : Chu3UserLinked<UserActivity> {
    fun findTopByUserAndActivityIdAndKindOrderByIdDesc(user: Chu3UserData, activityId: Int, kind: Int): Optional<UserActivity>

    fun findAllByUser_Card_ExtIdAndKindOrderBySortNumberDesc(extId: Long, kind: Int): List<UserActivity>
}

interface Chu3UserCardPrintStateRepo : Chu3UserLinked<UserCardPrintState> {
    fun findByUser_Card_ExtIdAndHasCompleted(extId: Long, hasCompleted: Boolean): List<UserCardPrintState>

    fun findByUserAndGachaIdAndHasCompleted(userData: Chu3UserData, gachaId: Int, hasCompleted: Boolean): List<UserCardPrintState>
}

interface Chu3UserCharacterRepo : Chu3UserLinked<UserCharacter> {
    fun findTopByUserAndCharacterIdOrderByIdDesc(user: Chu3UserData, characterId: Int): Optional<UserCharacter>
}

interface Chu3UserChargeRepo : Chu3UserLinked<UserCharge> {
    fun findByUserAndChargeId(extId: Chu3UserData, chargeId: Int): Optional<UserCharge>
}

interface Chu3UserCourseRepo : Chu3UserLinked<UserCourse> {
    fun findTopByUserAndCourseIdOrderByIdDesc(user: Chu3UserData, courseId: Int): Optional<UserCourse>
}

interface Chu3UserDataRepo : GenericUserDataRepo<Chu3UserData>

interface Chu3UserDuelRepo : Chu3UserLinked<UserDuel> {
    fun findTopByUserAndDuelIdOrderByIdDesc(user: Chu3UserData, duelId: Int): Optional<UserDuel>
}

interface Chu3UserGachaRepo : Chu3UserLinked<UserGacha> {
    fun findByUserAndGachaId(extId: Chu3UserData, gachaId: Int): Optional<UserGacha>
}

interface Chu3UserGameOptionRepo : Chu3UserLinked<UserGameOption>

interface Chu3UserGeneralDataRepo : Chu3UserLinked<UserGeneralData> {
    fun findByUserAndPropertyKey(user: Chu3UserData, key: String): Optional<UserGeneralData>

    fun findByUser_Card_ExtIdAndPropertyKey(extId: Long, key: String): Optional<UserGeneralData>
}

interface Chu3UserItemRepo : Chu3UserLinked<UserItem> {
    fun findTopByUserAndItemIdAndItemKindOrderByIdDesc(user: Chu3UserData, itemId: Int, itemKind: Int): Optional<UserItem>

    fun findAllByUser_Card_ExtIdAndItemKind(extId: Long, itemKind: Int, pageable: Pageable): Page<UserItem>

    fun findAllByUser_Card_ExtIdAndItemKind(extId: Long, itemKind: Int): List<UserItem>
}

interface Chu3UserMapRepo : Chu3UserLinked<UserMap> {
    fun findTopByUserAndMapAreaIdOrderByIdDesc(user: Chu3UserData, mapAreaId: Int): Optional<UserMap>
}

interface Chu3UserMusicDetailRepo : Chu3UserLinked<UserMusicDetail> {
    fun findTopByUserAndMusicIdAndLevelOrderByIdDesc(user: Chu3UserData, musicId: Int, level: Int): Optional<UserMusicDetail>

    fun findByUser_Card_ExtIdAndMusicId(extId: Long, musicId: Int): List<UserMusicDetail>
}

interface Chu3UserPlaylogRepo : GenericPlaylogRepo<UserPlaylog>, Chu3UserLinked<UserPlaylog> {
    fun findByUser_Card_ExtIdAndLevelNot(extId: Long, levelNot: Int, page: Pageable): List<UserPlaylog>

    fun findByUser_Card_ExtIdAndMusicIdAndLevel(extId: Long, musicId: Int, level: Int): List<UserPlaylog>
}

interface Chu3GameAvatarAccRepo : JpaRepository<AvatarAcc, Long>

interface Chu3GameCharacterRepo : JpaRepository<Character, Long>

interface Chu3GameChargeRepo : JpaRepository<GameCharge, Long>

interface Chu3GameEventRepo : JpaRepository<GameEvent, Int> {
    fun findByEnable(enable: Boolean): List<GameEvent>
}

interface Chu3GameFrameRepo : JpaRepository<Frame, Long>

interface Chu3GameGachaCardRepo : JpaRepository<GameGachaCard, Long> {
    fun findAllByGachaId(gachaId: Int): List<GameGachaCard>
}

interface Chu3GameGachaRepo : JpaRepository<GameGacha, Long>

interface Chu3GameLoginBonusPresetsRepo : JpaRepository<GameLoginBonusPreset, Int> {
    @Query(
        value = "select * from chusan_game_login_bonus_preset where version = ?1 and is_enabled = ?2",
        nativeQuery = true
    )
    fun findLoginBonusPresets(version: Int, isEnabled: Int): List<GameLoginBonusPreset>
}

interface Chu3GameLoginBonusRepo : JpaRepository<GameLoginBonus, Int> {
    @Query(
        value = "select * from chusan_game_login_bonus where version = ?1 and preset_id = ?2 order by need_login_day_count desc",
        nativeQuery = true
    )
    fun findGameLoginBonus(version: Int, presetId: Int): List<GameLoginBonus>

    @Query(
        value = "select * from chusan_game_login_bonus where version = ?1 and preset_id = ?2 and need_login_day_count = ?3 limit 1",
        nativeQuery = true
    )
    fun findByRequiredDays(version: Int, presetId: Int, requiredDays: Int): Optional<GameLoginBonus>
}

interface Chu3GameMapIconRepo : JpaRepository<MapIcon, Long>

interface Chu3GameMusicRepo : JpaRepository<Music, Long> {
    fun findByMusicId(musicId: Int): Optional<Music>
}

interface Chu3GameNamePlateRepo : JpaRepository<NamePlate, Long>

interface Chu3GameSystemVoiceRepo : JpaRepository<SystemVoice, Long>

interface Chu3GameTrophyRepo : JpaRepository<Trophy, Long>

@Component
class Chu3Repos(
    val userLoginBonus: Chu3UserLoginBonusRepo,
    val userActivity: Chu3UserActivityRepo,
    val userCardPrintState: Chu3UserCardPrintStateRepo,
    val userCharacter: Chu3UserCharacterRepo,
    val userCharge: Chu3UserChargeRepo,
    val userCourse: Chu3UserCourseRepo,
    val userData: Chu3UserDataRepo,
    val userDuel: Chu3UserDuelRepo,
    val userGacha: Chu3UserGachaRepo,
    val userGameOption: Chu3UserGameOptionRepo,
    val userGeneralData: Chu3UserGeneralDataRepo,
    val userItem: Chu3UserItemRepo,
    val userMap: Chu3UserMapRepo,
    val userMusicDetail: Chu3UserMusicDetailRepo,
    val userPlaylog: Chu3UserPlaylogRepo,
    val gameAvatarAcc: Chu3GameAvatarAccRepo,
    val gameCharacter: Chu3GameCharacterRepo,
    val gameCharge: Chu3GameChargeRepo,
    val gameEvent: Chu3GameEventRepo,
    val gameFrame: Chu3GameFrameRepo,
    val gameGachaCard: Chu3GameGachaCardRepo,
    val gameGacha: Chu3GameGachaRepo,
    val gameLoginBonusPresets: Chu3GameLoginBonusPresetsRepo,
    val gameLoginBonus: Chu3GameLoginBonusRepo,
    val gameMapIcon: Chu3GameMapIconRepo,
    val gameMusic: Chu3GameMusicRepo,
    val gameNamePlate: Chu3GameNamePlateRepo,
    val gameSystemVoice: Chu3GameSystemVoiceRepo,
    val gameTrophy: Chu3GameTrophyRepo
)