package icu.samnyan.aqua.sega.chusan

import ext.*
import icu.samnyan.aqua.net.db.AquaUserServices
import icu.samnyan.aqua.net.utils.simpleDescribe
import icu.samnyan.aqua.sega.chusan.handler.*
import icu.samnyan.aqua.sega.chusan.model.Chu3Repos
import icu.samnyan.aqua.sega.chusan.model.request.UserCMissionResp
import icu.samnyan.aqua.sega.chusan.model.response.data.MatchingMemberInfo
import icu.samnyan.aqua.sega.chusan.model.response.data.MatchingWaitState
import icu.samnyan.aqua.sega.chusan.model.userdata.UserCharge
import icu.samnyan.aqua.sega.general.BaseHandler
import icu.samnyan.aqua.sega.general.RequestContext
import icu.samnyan.aqua.sega.general.SpecialHandler
import icu.samnyan.aqua.sega.general.toSpecial
import icu.samnyan.aqua.sega.util.jackson.StringMapper
import icu.samnyan.aqua.spring.Metrics
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
    val getUserLoginBonus: GetUserLoginBonusHandler,
    val getUserMusic: GetUserMusicHandler,
    val getUserRecentRating: GetUserRecentRatingHandler,
    val getUserTeam: GetUserTeamHandler,
    val upsertUserAll: UpsertUserAllHandler,
    val cmGetUserPreview: CMGetUserPreviewHandler,
    val cmGetUserData: CMGetUserDataHandler,
    val cmGetUserCharacter: CMGetUserCharacterHandler,
    val cmGetUserItem: CMGetUserItemHandler,
    val cmUpsertUserGacha: CMUpsertUserGachaHandler,
    val cmUpsertUserPrintSubtract: CMUpsertUserPrintSubtractHandler,
    val cmUpsertUserPrintCancel: CMUpsertUserPrintCancelHandler,

    val mapper: StringMapper,
    val db: Chu3Repos,
    val us: AquaUserServices,
    val versionHelper: ChusanVersionHelper,
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
    infix operator fun String.invoke(fn: SpecialHandler) = initH.set(this.lowercase(), fn)
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
        logger.info("Chu3 < $api : ${data.toJson()}")

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


@Suppress("UNCHECKED_CAST")
fun ChusanServletController.init() {
    // Stub handlers
    "GetGameRanking" { """{"type":"${data["type"]}","length":"0","gameRankingList":[]}""" }
    "GetGameIdlist" { """{"type":"${data["type"]}","length":"0","gameIdlistList":[]}""" }

    "GetTeamCourseSetting" { """{"userId":"${data["userId"]}","length":"0","nextIndex":"0","teamCourseSettingList":[]}""" }
    "GetTeamCourseRule" { """{"userId":"${data["userId"]}","length":"0","nextIndex":"0","teamCourseRuleList":[]}""" }
    "GetUserCtoCPlay" { """{"userId":"${data["userId"]}","orderBy":"0","count":"0","userCtoCPlayList":[]}""" }
    "GetUserRivalMusic" { """{"userId":"${data["userId"]}","rivalId":"0","length":"0","nextIndex":"0","userRivalMusicList":[]}""" }
    "GetUserRivalData" { """{"userId":"${data["userId"]}","length":"0","userRivalData":[]}""" }
    "GetUserRegion" { """{"userId":"${data["userId"]}","length":"0","userRegionList":[]}""" }
    "GetUserPrintedCard" { """{"userId":"${data["userId"]}","length":0,"nextIndex":-1,"userPrintedCardList":[]}""" }
    "GetUserSymbolChatSetting" { """{"userId":"${data["userId"]}","length":"0","symbolChatInfoList":[]}""" }
    "GetUserNetBattleData" { """{"userId":"${data["userId"]}","userNetBattleData":{"recentNBSelectMusicList":[],"recentNBMusicList":[]}}""" }
    "GetUserNetBattleRankingInfo" { """{"userId":"${data["userId"]}","length":"0","userNetBattleRankingInfoList":{}}""" }

    "CMUpsertUserPrint" { """{"returnCode":1,"orderId":"0","serialId":"FAKECARDIMAG12345678","apiName":"CMUpsertUserPrintApi"}""" }
    "CMUpsertUserPrintlog" { """{"returnCode":1,"orderId":"0","serialId":"FAKECARDIMAG12345678","apiName":"CMUpsertUserPrintlogApi"}""" }

    // Matching TODO: Actually implement this
    "EndMatching" { """{"matchingResult":{"matchingMemberInfoList":[],"matchingMemberRoleList":[],"reflectorUri":""}}""" }
    "GetMatchingState" { """{"matchingWaitState":{"restMSec":"30000","pollingInterval":"10","matchingMemberInfoList":[],"isFinish":"true"}}""" }

    "BeginMatching" {
        val memberInfo = parsing { mapper.convert<MatchingMemberInfo>(data["matchingMemberInfo"] as JDict) }
        mapOf("roomId" to 1, "matchingWaitState" to MatchingWaitState(listOf(memberInfo)))
    }

    // User handlers
    "GetUserData" {
        val user = db.userData.findByCard_ExtId(uid)() ?: (400 - "User not found")
        mapOf("userId" to uid, "userData" to user)
    }
    "GetUserOption" {
        val userGameOption = db.userGameOption.findSingleByUser_Card_ExtId(uid)() ?: (400 - "User not found")
        mapOf("userId" to uid, "userGameOption" to userGameOption)
    }
    "GetUserActivity" {
        val kind = parsing { data["kind"]!!.int }
        val a = db.userActivity.findAllByUser_Card_ExtIdAndKind(uid, kind).sortedBy { -it.sortNumber }
        mapOf("userId" to uid, "length" to a.size, "kind" to kind, "userActivityList" to a)
    }

    "GetUserCharge" {
        val lst = db.userCharge.findByUser_Card_ExtId(uid)
        mapOf("userId" to uid, "length" to lst.size, "userChargeList" to lst)
    }

    "GetUserDuel" {
        val lst = db.userDuel.findByUser_Card_ExtId(uid)
        mapOf("userId" to uid, "length" to lst.size, "userDuelList" to lst)
    }

    "GetUserGacha" {
        val lst = db.userGacha.findByUser_Card_ExtId(uid)
        mapOf("userId" to uid, "length" to lst.size, "userGachaList" to lst)
    }

    "RollGacha" {
        val (gachaId, times) = parsing { data["gachaId"]!!.int to data["times"]!!.int }
        val lst = db.gameGachaCard.findAllByGachaId(gachaId).shuffled().take(times)
        mapOf("length" to lst.size, "gameGachaCardList" to lst)
    }

    "GetGameGachaCardById" { db.gameGachaCard.findAllByGachaId(parsing { data["gachaId"]!!.int }).let {
        mapOf("gachaId" to it.size, "length" to it.size, "isPickup" to false, "gameGachaCardList" to it,
            "emissionList" to empty, "afterCalcList" to empty)
    } }

    "GetUserCMission" {
        parsing { UserCMissionResp().apply {
            userId = uid
            missionId = parsing { data["missionId"]!!.int }
        } }.apply {
            db.userCMission.findByUser_Card_ExtIdAndMissionId(uid, missionId)()?.let {
                point = it.point
                userCMissionProgressList = db.userCMissionProgress.findByUser_Card_ExtIdAndMissionId(uid, missionId)
            }
        }
    }

    "GetUserCardPrintError" {
        val lst = db.userCardPrintState.findByUser_Card_ExtIdAndHasCompleted(uid, false)
        mapOf("userId" to uid, "length" to lst.size, "userCardPrintStateList" to lst)
    }

    "GetUserCharacter" {
        // Let's try not paging at all
        val lst = db.userCharacter.findByUser_Card_ExtId(uid)
        mapOf("userId" to uid, "length" to lst.size, "nextIndex" to -1, "userCharacterList" to lst)
    }

    "GetUserCourse" {
        val lst = db.userCourse.findByUser_Card_ExtId(uid)
        mutableMapOf("userId" to uid, "length" to lst.size, "userCourseList" to lst).apply {
            if (data.containsKey("nextIndex")) this["nextIndex"] = -1
        }
    }

    "GetUserItem" {
        val kind = parsing { (data["nextIndex"]!!.long % 10000000000L).int }
        val lst = db.userItem.findAllByUser_Card_ExtIdAndItemKind(uid, kind)
        mapOf("userId" to uid, "length" to lst.size, "nextIndex" to -1, "itemKind" to kind, "userItemList" to lst)
    }

    "GetUserFavoriteItem" {
        val kind = parsing { data["kind"]!!.int }

        // TODO: Actually store this info at UpsertUserAll
        val fav = when (kind) {
            1 -> "favorite_music"
            3 -> "favorite_chara"
            else -> null
        }?.let { db.userGeneralData.findByUser_Card_ExtIdAndPropertyKey(uid, it)() }?.propertyValue

        val lst = fav?.let {
            if (it.isNotBlank() && it.contains(",")) it.split(",").map { it.int }
            else null
        } ?: emptyList()

        mapOf("userId" to uid, "kind" to kind, "length" to lst.size, "nextIndex" to -1, "userFavoriteItemList" to lst)
    }

    val userPreviewKeys = ("userName,reincarnationNum,level,exp,playerRating,lastGameId,lastRomVersion," +
        "lastDataVersion,trophyId,classEmblemMedal,classEmblemBase,battleRankId").split(',').toSet()

    "GetUserPreview" {
        val user = db.userData.findByCard_ExtId(uid)() ?: (400 - "User not found")
        val chara = db.userCharacter.findByUserAndCharacterId(user, user.characterId)
        val option = db.userGameOption.findSingleByUser(user)()
        val userDict = user.toJson().jsonMap().filterKeys { it in userPreviewKeys }

        mapOf(
            "userId" to uid, "isLogin" to false, "emoneyBrandId" to 0,
            "lastLoginDate" to user.lastLoginDate, "lastPlayDate" to user.lastPlayDate,
            "userCharacter" to chara,
            "playerLevel" to option?.playerLevel,
            "rating" to option?.rating,
            "headphone" to option?.headphone,
            "chargeState" to 1, "userNameEx" to "", "banState" to 0,
        ) + userDict
    }

    "GetUserMapArea" {
        val maps = parsing { data["mapAreaIdList"] as List<Map<String, String>> }
            .mapNotNull { it["mapAreaId"]?.toIntOrNull() }

        mapOf("userId" to uid, "userMapAreaList" to db.userMap.findAllByUserCardExtIdAndMapAreaIdIn(uid, maps))
    }

    // Game settings
    "GetGameSetting" {
        val version = data["version"].toString()

        // Fixed reboot time triggers chusan maintenance lockout, so let's try minime method which sets it dynamically
        // Special thanks to skogaby
        // Hardcode so that the reboot time always started 3 hours ago and ended 2 hours ago
        val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")

        // Get the request url as te address
        val addr = (req.getHeader("wrapper original url") ?: req.requestURL.toString())
            .removeSuffix("GetGameSettingApi")

        mapOf(
            "gameSetting" to mapOf(
                "romVersion" to "$version.00",  // Chusan checks these two versions to determine if it can enable game modes
                "dataVersion" to versionHelper[data["clientId"].toString()],
                "isMaintenance" to false,
                "requestInterval" to 0,
                "rebootStartTime" to LocalDateTime.now().minusHours(3).format(fmt),
                "rebootEndTime" to LocalDateTime.now().minusHours(2).format(fmt),
                "isBackgroundDistribute" to false,
                "maxCountCharacter" to 300,
                "maxCountItem" to 300,
                "maxCountMusic" to 300,
                "matchStartTime" to LocalDateTime.now().minusHours(1).format(fmt),
                "matchEndTime" to LocalDateTime.now().plusHours(1).format(fmt),
                "matchTimeLimit" to 10,
                "matchErrorLimit" to 10,
                "matchingUri" to addr,
                "matchingUriX" to addr,
                "udpHolePunchUri" to addr,
                "reflectorUri" to addr
            ),
            "isDumpUpload" to false,
            "isAou" to false
        )
    }

    // Upserts
    "UpsertUserChargelog" {
        val charge = parsing { mapper.convert<UserCharge>(data["userCharge"] as JDict) }
        charge.user = db.userData.findByCard_ExtId(uid)() ?: (400 - "User not found")
        charge.id = db.userCharge.findByUser_Card_ExtIdAndChargeId(uid, charge.chargeId)?.id ?: 0
        db.userCharge.save(charge)
        """{"returnCode":"1"}"""
    }

    // Static
    "GetGameEvent" static { db.gameEvent.findByEnable(true).let { mapOf("type" to 1, "length" to it.size, "gameEventList" to it) } }
    "GetGameCharge" static { db.gameCharge.findAll().let { mapOf("length" to it.size, "gameChargeList" to it) } }
    "GetGameGacha" static { db.gameGacha.findAll().let { mapOf("length" to it.size, "gameGachaList" to it, "registIdList" to empty) } }
    "GetGameMapAreaCondition" static {
        mapOf(
            "gameMapAreaConditionList" to listOf(
                mapOf("mapAreaId" to 2206201, "mapAreaConditionList" to listOf(
                    mapOf("type" to 3, "conditionId" to 6832, "logicalOpe" to 1)
                )),
                mapOf("mapAreaId" to 2206203, "mapAreaConditionList" to listOf(
                    mapOf("type" to 3, "conditionId" to 6833, "logicalOpe" to 1)
                )),
                mapOf("mapAreaId" to 2206204, "mapAreaConditionList" to listOf(
                    mapOf("type" to 3, "conditionId" to 6834, "logicalOpe" to 1),
                    mapOf("type" to 3, "conditionId" to 6835, "logicalOpe" to 1)
                )),
                mapOf("mapAreaId" to 2206205, "mapAreaConditionList" to listOf(
                    mapOf("type" to 3, "conditionId" to 6837, "logicalOpe" to 1)
                )),
                mapOf("mapAreaId" to 2206206, "mapAreaConditionList" to listOf(
                    mapOf("type" to 3, "conditionId" to 6838, "logicalOpe" to 1)
                )),
                mapOf("mapAreaId" to 2206207, "mapAreaConditionList" to listOf(
                    mapOf("type" to 2, "conditionId" to 2206201, "logicalOpe" to 1),
                    mapOf("type" to 2, "conditionId" to 2206202, "logicalOpe" to 1),
                    mapOf("type" to 2, "conditionId" to 2206203, "logicalOpe" to 1),
                    mapOf("type" to 2, "conditionId" to 2206204, "logicalOpe" to 1),
                    mapOf("type" to 2, "conditionId" to 2206205, "logicalOpe" to 1),
                    mapOf("type" to 2, "conditionId" to 2206206, "logicalOpe" to 1)
                )),
                mapOf("mapAreaId" to 3229301, "mapAreaConditionList" to listOf(
                    mapOf("type" to 1, "conditionId" to 3020701, "logicalOpe" to 2)
                )),
                mapOf("mapAreaId" to 3229302, "mapAreaConditionList" to listOf(
                    mapOf("type" to 1, "conditionId" to 3020701, "logicalOpe" to 1)
                ))
            )
        )
    }
}
