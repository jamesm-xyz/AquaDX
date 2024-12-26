package icu.samnyan.aqua.sega.chusan.handler

import ext.*
import icu.samnyan.aqua.sega.chusan.ChusanVersionHelper
import icu.samnyan.aqua.sega.chusan.model.Chu3Repos
import icu.samnyan.aqua.sega.chusan.model.request.UpsertUserAll
import icu.samnyan.aqua.sega.chusan.model.userdata.UserCMission
import icu.samnyan.aqua.sega.chusan.model.userdata.UserCMissionProgress
import icu.samnyan.aqua.sega.chusan.model.userdata.UserGeneralData
import icu.samnyan.aqua.sega.chusan.model.userdata.UserLoginBonus
import icu.samnyan.aqua.sega.general.BaseHandler
import icu.samnyan.aqua.sega.general.dao.CardRepository
import icu.samnyan.aqua.sega.general.model.response.UserRecentRating
import icu.samnyan.aqua.sega.util.jackson.StringMapper
import lombok.AllArgsConstructor
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

/**
 * The handler for save user data. Only send in the end of the session.
 *
 * @author samnyan (privateamusement@protonmail.com)
 */
@AllArgsConstructor
@Component("ChusanUpsertUserAllHandler")
class UpsertUserAllHandler(
    val mapper: StringMapper,
    val rp: Chu3Repos,
    val cardRepo: CardRepository,
    val versionHelper: ChusanVersionHelper,
) : BaseHandler {
    val logger = logger()

    override fun handle(request: Map<String, Any>): Any? {
        val ext = request["userId"]?.long ?: return null
        val req = mapper.convert(request["upsertUserAll"], UpsertUserAll::class.java)

        req.run {
            // UserData
            val oldUser = rp.userData.findByCard_ExtId(ext)()
            val u = (userData?.get(0) ?: return null).apply {
                id = oldUser?.id ?: 0
                card = oldUser?.card ?: cardRepo.findByExtId(ext).expect("Card not found")
                userName = String(userName.toByteArray(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)
                userNameEx = ""
            }.also { rp.userData.saveAndFlush(it) }

            versionHelper[u.lastClientId] = u.lastDataVersion

            // Set users
            listOfNotNull(
                userPlaylogList, userGameOption, userMapAreaList, userCharacterList, userItemList,
                userMusicDetailList, userActivityList, userChargeList, userCourseList, userDuelList,
            ).flatten().forEach { it.user = u }

            // Ratings
            fun Iterable<UserRecentRating>.str() = joinToString(",") { "${it.musicId}:${it.difficultId}:${it.score}" }

            ls(
                userRecentRatingList to "recent_rating_list", userRatingBaseList to "rating_base_list",
                userRatingBaseHotList to "rating_hot_list", userRatingBaseNextList to "rating_next_list",
            ).filter { it.first != null }.forEach { (list, key) ->
                val d = rp.userGeneralData.findByUserAndPropertyKey(u, key)()
                    ?: UserGeneralData().apply { user = u; propertyKey = key }
                rp.userGeneralData.save(d.apply { propertyValue = list!!.str() })
            }

            // Playlog
            userPlaylogList?.let { rp.userPlaylog.saveAll(it) }

            // List data
            userGameOption?.get(0)?.let { obj ->
                rp.userGameOption.saveAndFlush(obj.apply {
                    id = rp.userGameOption.findSingleByUser(u)()?.id ?: 0 }) }

            userMapAreaList?.let { list ->
                rp.userMap.saveAll(list.distinctBy { it.mapAreaId }.mapApply {
                    id = rp.userMap.findByUserAndMapAreaId(u, mapAreaId)?.id ?: 0 }) }

            userCharacterList?.let { list ->
                rp.userCharacter.saveAll(list.distinctBy { it.characterId }.mapApply {
                    id = rp.userCharacter.findByUserAndCharacterId(u, characterId)?.id ?: 0 }) }

            userItemList?.let { list ->
                rp.userItem.saveAll(list.distinctBy { it.itemId to it.itemKind }.mapApply {
                    id = rp.userItem.findByUserAndItemIdAndItemKind(u, itemId, itemKind)?.id ?: 0 }) }

            userMusicDetailList?.let { list ->
                rp.userMusicDetail.saveAll(list.distinctBy { it.musicId to it.level }.mapApply {
                    id = rp.userMusicDetail.findByUserAndMusicIdAndLevel(u, musicId, level)?.id ?: 0 }) }

            userActivityList?.let { list ->
                rp.userActivity.saveAll(list.distinctBy { it.activityId to it.kind }.mapApply {
                    id = rp.userActivity.findByUserAndActivityIdAndKind(u, activityId, kind)?.id ?: 0 }) }

            userChargeList?.let { list ->
                rp.userCharge.saveAll(list.distinctBy { it.chargeId }.mapApply {
                    id = rp.userCharge.findByUserAndChargeId(u, chargeId)()?.id ?: 0 }) }

            userCourseList?.let { list ->
                rp.userCourse.saveAll(list.distinctBy { it.courseId }.mapApply {
                    id = rp.userCourse.findByUserAndCourseId(u, courseId)?.id ?: 0 }) }

            userDuelList?.let { list ->
                rp.userDuel.saveAll(list.distinctBy { it.duelId }.mapApply {
                    id = rp.userDuel.findByUserAndDuelId(u, duelId)?.id ?: 0 }) }

            // Need testing
            userLoginBonusList?.let { list ->
                rp.userLoginBonus.saveAll(list.distinctBy { it["presetId"] as String }.map {
                    val id = it["presetId"]!!.int
                    (rp.userLoginBonus.findLoginBonus(ext.int, 1, id)() ?: UserLoginBonus()).apply {
                        user = u.id.toInt()
                        presetId = id
                        lastUpdateDate = LocalDateTime.now()
                        isWatched = true
                    }
                })
            }

            req.userCMissionList?.forEach { d ->
                (rp.userCMission.findByUser_Card_ExtIdAndMissionId(ext, d.missionId)()
                    ?: UserCMission().apply {
                        missionId = d.missionId
                        user = u
                    }
                ).apply { point = d.point }.also { rp.userCMission.save(it) }

                d.userCMissionProgressList?.forEach inner@ { p ->
                    (rp.userCMissionProgress.findByUser_Card_ExtIdAndMissionIdAndOrder(ext, d.missionId, p.order)()
                        ?: UserCMissionProgress().apply {
                            missionId = d.missionId
                            order = p.order
                            user = u
                        }
                    ).apply {
                        progress = p.progress
                        stage = p.stage
                    }.also { rp.userCMissionProgress.save(it) }
                }
            }
        }

        return """{"returnCode":1}"""
    }
}
