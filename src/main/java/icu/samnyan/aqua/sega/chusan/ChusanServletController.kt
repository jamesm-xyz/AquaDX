package icu.samnyan.aqua.sega.chusan

import ext.*
import icu.samnyan.aqua.net.utils.simpleDescribe
import icu.samnyan.aqua.sega.chusan.handler.*
import icu.samnyan.aqua.sega.chusan.model.Chu3Repos
import icu.samnyan.aqua.sega.chusan.model.request.UserCMissionResp
import icu.samnyan.aqua.sega.general.BaseHandler
import icu.samnyan.aqua.sega.general.RequestContext
import icu.samnyan.aqua.sega.general.SpecialHandler
import icu.samnyan.aqua.sega.general.toSpecial
import icu.samnyan.aqua.sega.util.jackson.StringMapper
import icu.samnyan.aqua.spring.Metrics
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController
import kotlin.collections.set
import kotlin.reflect.full.declaredMemberProperties

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Suppress("unused")
@RestController
@API(value = ["/g/chu3/{version}/ChuniServlet", "/g/chu3/{version}"])
class ChusanServletController(
    val gameLogin: GameLoginHandler,
    val getGameSetting: GetGameSettingHandler,
    val getUserCharacter: GetUserCharacterHandler,
    val getUserCourse: GetUserCourseHandler,
    val getUserFavoriteItem: GetUserFavoriteItemHandler,
    val getUserItem: GetUserItemHandler,
    val getUserLoginBonus: GetUserLoginBonusHandler,
    val getUserMapArea: GetUserMapAreaHandler,
    val getUserMusic: GetUserMusicHandler,
    val getUserPreview: GetUserPreviewHandler,
    val getUserRecentRating: GetUserRecentRatingHandler,
    val getUserTeam: GetUserTeamHandler,
    val upsertUserAll: UpsertUserAllHandler,
    val upsertUserChargelog: UpsertUserChargelogHandler,
    val getUserCardPrintError: GetUserCardPrintErrorHandler,
    val cmGetUserPreview: CMGetUserPreviewHandler,
    val cmGetUserData: CMGetUserDataHandler,
    val cmGetUserCharacter: CMGetUserCharacterHandler,
    val getUserGacha: GetUserGachaHandler,
    val cmGetUserItem: CMGetUserItemHandler,
    val rollGacha: RollGachaHandler,
    val cmUpsertUserGacha: CMUpsertUserGachaHandler,
    val cmUpsertUserPrintSubtract: CMUpsertUserPrintSubtractHandler,
    val cmUpsertUserPrintCancel: CMUpsertUserPrintCancelHandler,
    val beginMatching: BeginMatchingHandler,

    // Luminous
    val getGameMapAreaCondition: GetGameMapAreaConditionHandler,

    val mapper: StringMapper,
    val db: Chu3Repos,
) {
    val logger = LoggerFactory.getLogger(ChusanServletController::class.java)

    // Below are code related to handling the handlers
    val endpointList = mutableListOf(
        "GameLoginApi", "GetGameChargeApi", "GetGameEventApi", "GetGameIdlistApi",
        "GetGameRankingApi", "GetGameSettingApi", "GetTeamCourseRuleApi", "GetTeamCourseSettingApi", "GetUserActivityApi",
        "GetUserCharacterApi", "GetUserChargeApi", "GetUserCourseApi", "GetUserDataApi", "GetUserDuelApi",
        "GetUserFavoriteItemApi", "GetUserItemApi", "GetUserLoginBonusApi", "GetUserMapAreaApi", "GetUserMusicApi",
        "GetUserOptionApi", "GetUserPreviewApi", "GetUserRecentRatingApi", "GetUserRegionApi", "GetUserRivalDataApi",
        "GetUserRivalMusicApi", "GetUserTeamApi", "GetUserSymbolChatSettingApi", "GetUserNetBattleDataApi",
        "UpsertUserAllApi", "UpsertUserChargelogApi", "GetGameGachaApi",
        "MatchingServer/BeginMatchingApi", "MatchingServer/EndMatchingApi", "MatchingServer/GetMatchingStateApi",
        "GetGameGachaCardByIdApi", "GetUserCardPrintErrorApi", "CMGetUserCharacterApi", "CMGetUserDataApi",
        "GetUserGachaApi", "CMGetUserItemApi", "CMGetUserPreviewApi", "GetUserPrintedCardApi",
        "RollGachaApi", "CMUpsertUserGachaApi", "CMUpsertUserPrintApi", "CMUpsertUserPrintCancelApi",
        "CMUpsertUserPrintlogApi", "CMUpsertUserPrintSubtractApi",
        "GetUserCtoCPlayApi", "GetUserCMissionApi", "GetUserNetBattleRankingInfoApi", "GetGameMapAreaConditionApi")

    val noopEndpoint = setOf("UpsertClientBookkeepingApi", "UpsertClientDevelopApi", "UpsertClientErrorApi",
        "UpsertClientSettingApi", "UpsertClientTestmodeApi", "CreateTokenApi", "RemoveTokenApi", "UpsertClientUploadApi",
        "MatchingServer/Ping", "PrinterLoginApi", "PrinterLogoutApi", "Ping", "GameLogoutApi",
        "MatchingServer/RemoveMatchingMemberApi")

    val matchingEndpoints = (endpointList + noopEndpoint).filter { it.startsWith("MatchingServer") }
        .map { it.split("/").last() }.toSet()

    // Fun!
    val initH = mutableMapOf<String, SpecialHandler>()
    infix fun String.special(fn: SpecialHandler) = initH.set(this.lowercase(), fn)
    operator fun String.invoke(fn: (Map<String, Any>) -> Any) = this special { fn(it.data) }
    infix fun String.user(fn: (Map<String, Any>, Long) -> Any) = this { fn(it, parsing { it["userId"]!!.long }) }
    infix fun String.static(fn: () -> Any) = mapper.write(fn()).let { resp -> this { resp } }
    val meow = init()

    val members = this::class.declaredMemberProperties
    val handlers: Map<String, SpecialHandler> = endpointList.associateWith { api ->
        val name = api.replace("Api", "").replace("MatchingServer/", "").lowercase()
        initH[name]?.let { return@associateWith it }
        (members.find { it.name.lowercase() == name } ?: members.find { it.name.lowercase() == name.replace("cm", "") })
            ?.let { (it.call(this) as BaseHandler).toSpecial() }
            ?: throw IllegalArgumentException("Chu3: No handler found for $api")
    }

    @API("/{endpoint}", "/MatchingServer/{endpoint}")
    fun handle(@PV endpoint: Str, @RB data: MutableMap<Str, Any>, @PV version: Str, req: HttpServletRequest): Any {
        val ctx = RequestContext(req, data)
        var api = endpoint
        data["version"] = version

        // Export version
        if (api.endsWith("C3Exp")) {
            api = api.removeSuffix("C3Exp")
            data["c3exp"] = true
        }
        if (api in matchingEndpoints) api = "MatchingServer/$api"

        if (api !in noopEndpoint && !handlers.containsKey(api)) {
            logger.warn("Chu3 > $api not found")
            return """{"returnCode":"1","apiName":"$api"}"""
        }

        // Only record the counter metrics if the API is known.
        Metrics.counter("aquadx_chusan_api_call", "api" to api).increment()
        if (api in noopEndpoint) {
            logger.info("Chu3 > $api no-op")
            return """{"returnCode":"1"}"""
        }
        logger.info("Chu3 < $api : $data")

        return try {
            Metrics.timer("aquadx_chusan_api_latency", "api" to api).recordCallable {
                handlers[api]!!(ctx).let { if (it is String) it else mapper.write(it) }.also {
                    if (api !in setOf("GetUserItemApi", "GetGameEventApi"))
                        logger.info("Chu3 > $api : $it")
                }
            }
        } catch (e: Exception) {
            Metrics.counter(
                "aquadx_chusan_api_error",
                "api" to api, "error" to e.simpleDescribe()
            ).increment()
            throw e
        }
    }
}


fun ChusanServletController.init() {
    // Stub handlers
    "GetGameRanking" { """{"type":"${it["type"]}","length":"0","gameRankingList":[]}""" }
    "GetGameIdlist" { """{"type":"${it["type"]}","length":"0","gameIdlistList":[]}""" }

    "GetTeamCourseSetting" { """{"userId":"${it["userId"]}","length":"0","nextIndex":"0","teamCourseSettingList":[]}""" }
    "GetTeamCourseRule" { """{"userId":"${it["userId"]}","length":"0","nextIndex":"0","teamCourseRuleList":[]}""" }
    "GetUserCtoCPlay" { """{"userId":"${it["userId"]}","orderBy":"0","count":"0","userCtoCPlayList":[]}""" }
    "GetUserRivalMusic" { """{"userId":"${it["userId"]}","rivalId":"0","length":"0","nextIndex":"0","userRivalMusicList":[]}""" }
    "GetUserRivalData" { """{"userId":"${it["userId"]}","length":"0","userRivalData":[]}""" }
    "GetUserRegion" { """{"userId":"${it["userId"]}","length":"0","userRegionList":[]}""" }
    "GetUserPrintedCard" { """{"userId":"${it["userId"]}","length":0,"nextIndex":-1,"userPrintedCardList":[]}""" }
    "GetUserSymbolChatSetting" { """{"userId":"${it["userId"]}","length":"0","symbolChatInfoList":[]}""" }
    "GetUserNetBattleData" { """{"userId":"${it["userId"]}","userNetBattleData":{"recentNBSelectMusicList":[],"recentNBMusicList":[]}}""" }
    "GetUserNetBattleRankingInfo" { """{"userId":"${it["userId"]}","length":"0","userNetBattleRankingInfoList":{}}""" }

    "CMUpsertUserPrint" { """{"returnCode":1,"orderId":"0","serialId":"FAKECARDIMAG12345678","apiName":"CMUpsertUserPrintApi"}""" }
    "CMUpsertUserPrintlog" { """{"returnCode":1,"orderId":"0","serialId":"FAKECARDIMAG12345678","apiName":"CMUpsertUserPrintlogApi"}""" }

    // Matching
    "EndMatching" { """{"matchingResult":{"matchingMemberInfoList":[],"matchingMemberRoleList":[],"reflectorUri":""}}""" }
    "GetMatchingState" { """{"matchingWaitState":{"restMSec":"30000","pollingInterval":"10","matchingMemberInfoList":[],"isFinish":"true"}}""" }

    // User handlers
    "GetUserData" user { _, u ->
        val user = db.userData.findByCard_ExtId(u)() ?: (400 - "User not found")
        mapOf("userId" to u, "userData" to user)
    }
    "GetUserOption" user { _, u ->
        val userGameOption = db.userGameOption.findSingleByUser_Card_ExtId(u)() ?: (400 - "User not found")
        mapOf("userId" to u, "userGameOption" to userGameOption)
    }
    "GetUserActivity" user { req, u ->
        val kind = parsing { req["kind"]!!.int }
        val a = db.userActivity.findAllByUser_Card_ExtIdAndKind(u, kind).sortedBy { -it.sortNumber }
        mapOf("userId" to u, "length" to a.size, "kind" to kind, "userActivityList" to a)
    }
    "GetUserCharge" user { _, u -> db.userCharge.findByUser_Card_ExtId(u)
        .let { mapOf("userId" to u, "length" to it.size, "userChargeList" to it) }
    }
    "GetUserDuel" user { _, u -> db.userDuel.findByUser_Card_ExtId(u)
        .let { mapOf("userId" to u, "length" to it.size, "userDuelList" to it) }
    }

    // Other handlers
    "GetGameGachaCardById" { db.gameGachaCard.findAllByGachaId(parsing { it["gachaId"]!!.int }).let {
        mapOf("gachaId" to it.size, "length" to it.size, "isPickup" to false, "gameGachaCardList" to it, "emissionList" to empty, "afterCalcList" to empty)
    } }

    "GetUserCMission" user { req, u ->
        parsing { UserCMissionResp().apply {
            userId = u
            missionId = req["missionId"]!!.int
        } }.apply {
            db.userCMission.findByUser_Card_ExtIdAndMissionId(u, missionId)()?.let {
                point = it.point
                userCMissionProgressList = db.userCMissionProgress.findByUser_Card_ExtIdAndMissionId(u, missionId)
            }
        }
    }

    // Static
    "GetGameEvent" static { db.gameEvent.findByEnable(true).let { mapOf("type" to 1, "length" to it.size, "gameEventList" to it) } }
    "GetGameCharge" static { db.gameCharge.findAll().let { mapOf("length" to it.size, "gameChargeList" to it) } }
    "GetGameGacha" static { db.gameGacha.findAll().let { mapOf("length" to it.size, "gameGachaList" to it, "registIdList" to empty) } }
}
