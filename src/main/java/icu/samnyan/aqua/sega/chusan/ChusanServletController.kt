package icu.samnyan.aqua.sega.chusan

import ext.*
import icu.samnyan.aqua.net.utils.simpleDescribe
import icu.samnyan.aqua.sega.chusan.handler.*
import icu.samnyan.aqua.sega.general.BaseHandler
import icu.samnyan.aqua.sega.util.jackson.StringMapper
import icu.samnyan.aqua.spring.Metrics
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.full.declaredMemberProperties

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Suppress("unused")
@RestController
@API(value = ["/g/chu3/{version}/ChuniServlet", "/g/chu3/{version}"])
class ChusanServletController(
    val gameLogin: GameLoginHandler,
    val gameLogout: GameLogoutHandler,
    val getGameCharge: GetGameChargeHandler,
    val getGameEvent: GetGameEventHandler,
    val getGameSetting: GetGameSettingHandler,
    val getUserActivity: GetUserActivityHandler,
    val getUserCharacter: GetUserCharacterHandler,
    val getUserCharge: GetUserChargeHandler,
    val getUserCourse: GetUserCourseHandler,
    val getUserData: GetUserDataHandler,
    val getUserDuel: GetUserDuelHandler,
    val getUserFavoriteItem: GetUserFavoriteItemHandler,
    val getUserItem: GetUserItemHandler,
    val getUserLoginBonus: GetUserLoginBonusHandler,
    val getUserMapArea: GetUserMapAreaHandler,
    val getUserMusic: GetUserMusicHandler,
    val getUserOption: GetUserOptionHandler,
    val getUserPreview: GetUserPreviewHandler,
    val getUserRecentRating: GetUserRecentRatingHandler,
    val getUserNetBattleData: GetUserNetBattleDataHandler,
    val getUserTeam: GetUserTeamHandler,
    val upsertUserAll: UpsertUserAllHandler,
    val upsertUserChargelog: UpsertUserChargelogHandler,
    val getGameGacha: GetGameGachaHandler,
    val getGameGachaCardById: GetGameGachaCardByIdHandler,
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
    val removeMatchingMember: RemoveMatchingMemberHandler,

    // Luminous
    val getUserCMission: GetUserCMissionHandler,
    val getUserNetBattleRankingInfo: GetUserNetBattleRankingInfoHandler,
    val getGameMapAreaCondition: GetGameMapAreaConditionHandler,

    val mapper: StringMapper
) {
    val logger = LoggerFactory.getLogger(ChusanServletController::class.java)

    val getGameRanking = BaseHandler { """{"type":"${it["type"]}","length":"0","gameRankingList":[]}""" }
    val getGameIdlist = BaseHandler { """{"type":"${it["type"]}","length":"0","gameRankingList":[]}""" }

    val getTeamCourseSetting = BaseHandler { """{"userId":"${it["userId"]}","length":"0","nextIndex":"0","teamCourseSettingList":[]}""" }
    val getTeamCourseRule = BaseHandler { """{"userId":"${it["userId"]}","length":"0","nextIndex":"0","teamCourseRuleList":[]}""" }
    val getUserCtoCPlay = BaseHandler { """{"userId":"${it["userId"]}","orderBy":"0","count":"0","userCtoCPlayList":[]}""" }
    val getUserRivalMusic = BaseHandler { """{"userId":"${it["userId"]}","rivalId":"0","length":"0","nextIndex":"0","userRivalMusicList":[]}""" }
    val getUserRivalData = BaseHandler { """{"userId":"${it["userId"]}","length":"0","userRivalData":[]}""" }
    val getUserRegion = BaseHandler { """{"userId":"${it["userId"]}","length":"0","userRegionList":[]}""" }
    val getUserPrintedCard = BaseHandler { """{"userId":"${it["userId"]}","length":0,"nextIndex":-1,"userPrintedCardList":[]}""" }
    val getUserSymbolChatSetting = BaseHandler { """{"userId":"${it["userId"]}","length":"0","symbolChatInfoList":[]}""" }

    val cmUpsertUserPrint = BaseHandler { """{"returnCode":1,"orderId":"0","serialId":"FAKECARDIMAG12345678","apiName":"CMUpsertUserPrintApi"}""" }
    val cmUpsertUserPrintlog = BaseHandler { """{"returnCode":1,"orderId":"0","serialId":"FAKECARDIMAG12345678","apiName":"CMUpsertUserPrintlogApi"}""" }

    // Matching
    val endMatching = BaseHandler { """{"matchingResult":{"matchingMemberInfoList":[],"matchingMemberRoleList":[],"reflectorUri":""}}""" }
    val getMatchingState = BaseHandler { """{"matchingWaitState":{"restMSec":"30000","pollingInterval":"10","matchingMemberInfoList":[],"isFinish":"true"}}""" }


    // Below are code related to handling the handlers
    val endpointList = mutableListOf(
        "GameLoginApi", "GameLogoutApi", "GetGameChargeApi", "GetGameEventApi", "GetGameIdlistApi",
        "GetGameRankingApi", "GetGameSettingApi", "GetTeamCourseRuleApi", "GetTeamCourseSettingApi", "GetUserActivityApi",
        "GetUserCharacterApi", "GetUserChargeApi", "GetUserCourseApi", "GetUserDataApi", "GetUserDuelApi",
        "GetUserFavoriteItemApi", "GetUserItemApi", "GetUserLoginBonusApi", "GetUserMapAreaApi", "GetUserMusicApi",
        "GetUserOptionApi", "GetUserPreviewApi", "GetUserRecentRatingApi", "GetUserRegionApi", "GetUserRivalDataApi",
        "GetUserRivalMusicApi", "GetUserTeamApi", "GetUserSymbolChatSettingApi", "GetUserNetBattleDataApi",
        "UpsertClientBookkeepingApi", "UpsertClientDevelopApi", "UpsertClientErrorApi", "UpsertClientSettingApi",
        "UpsertClientTestmodeApi", "UpsertUserAllApi", "UpsertUserChargelogApi", "CreateTokenApi", "RemoveTokenApi",
        "UpsertClientUploadApi", "MatchingServer/Ping", "MatchingServer/BeginMatchingApi", "MatchingServer/EndMatchingApi",
        "MatchingServer/RemoveMatchingMemberApi", "MatchingServer/GetMatchingStateApi", "GetGameGachaApi",
        "GetGameGachaCardByIdApi", "GetUserCardPrintErrorApi", "CMGetUserCharacterApi", "CMGetUserDataApi",
        "GetUserGachaApi", "CMGetUserItemApi", "CMGetUserPreviewApi", "GetUserPrintedCardApi", "PrinterLoginApi",
        "PrinterLogoutApi", "RollGachaApi", "CMUpsertUserGachaApi", "CMUpsertUserPrintApi", "CMUpsertUserPrintCancelApi",
        "CMUpsertUserPrintlogApi", "CMUpsertUserPrintSubtractApi",

        // SDGS Exclusive
        "GetUserCtoCPlayApi", "GetUserCMissionApi", "GetUserNetBattleRankingInfoApi", "GetGameMapAreaConditionApi")
    val matchingEndpoints = endpointList.filter { it.startsWith("MatchingServer") }.map { it.split("/").last() }.toSet()

    val noopEndpoint = endpointList.popAll("UpsertClientBookkeepingApi", "UpsertClientDevelopApi", "UpsertClientErrorApi",
        "UpsertClientSettingApi", "UpsertClientTestmodeApi", "CreateTokenApi", "RemoveTokenApi", "UpsertClientUploadApi",
        "MatchingServer/Ping", "PrinterLoginApi", "PrinterLogoutApi", "Ping")

    val members = this::class.declaredMemberProperties
    val handlers: Map<String, BaseHandler> = endpointList.associateWith { api ->
        val name = api.replace("Api", "").replace("MatchingServer/", "").lowercase()
        (members.find { it.name.lowercase() == name } ?: members.find { it.name.lowercase() == name.replace("cm", "") })
            ?.let { it.call(this) as BaseHandler }
            ?: throw IllegalArgumentException("Chu3: No handler found for $api")
    }

    @API("/{endpoint}", "/MatchingServer/{endpoint}")
    fun handle(@PV endpoint: Str, @RB request: MutableMap<Str, Any>, @PV version: Str): Any {
        var api = endpoint
        request["version"] = version

        // Export version
        if (api.endsWith("C3Exp")) {
            api = api.removeSuffix("C3Exp")
            request["c3exp"] = true
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
        logger.info("Chu3 < $api : $request")

        return try {
            Metrics.timer("aquadx_chusan_api_latency", "api" to api).recordCallable {
                handlers[api]!!.handle(request).let { if (it is String) it else mapper.write(it) }.also {
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
