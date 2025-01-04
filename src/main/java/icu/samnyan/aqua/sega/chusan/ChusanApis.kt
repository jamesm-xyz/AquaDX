package icu.samnyan.aqua.sega.chusan

import ext.*
import icu.samnyan.aqua.sega.allnet.TokenChecker
import icu.samnyan.aqua.sega.chusan.model.request.UserCMissionResp
import icu.samnyan.aqua.sega.chusan.model.response.data.UserEmoney
import icu.samnyan.aqua.sega.chusan.model.userdata.UserCharge
import icu.samnyan.aqua.sega.chusan.model.userdata.UserItem
import icu.samnyan.aqua.sega.chusan.model.userdata.UserMusicDetail
import icu.samnyan.aqua.sega.general.model.response.UserRecentRating
import java.time.format.DateTimeFormatter

@Suppress("UNCHECKED_CAST")
fun ChusanController.chusanInit() {
    matchingApiInit()

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

    // User handlers
    "GetUserData" {
        db.userData.findByCard_ExtId(uid)()?.let{ u -> mapOf("userId" to uid, "userData" to u) }
    }
    "GetUserOption" {
        val userGameOption = db.userGameOption.findSingleByUser_Card_ExtId(uid)() ?: (400 - "User not found")
        mapOf("userId" to uid, "userGameOption" to userGameOption)
    }

    "RollGacha" {
        val (gachaId, times) = parsing { data["gachaId"]!!.int to data["times"]!!.int }
        val lst = db.gameGachaCard.findAllByGachaId(gachaId).shuffled().take(times)
        mapOf("length" to lst.size, "gameGachaCardList" to lst)
    }

    "GetGameGachaCardById" {
        val id = parsing { data["gachaId"]!!.int }
        db.gameGachaCard.findAllByGachaId(id).let {
            mapOf("gachaId" to id, "length" to it.size, "isPickup" to false, "gameGachaCardList" to it,
                "emissionList" to empty, "afterCalcList" to empty
            )
        }
    }

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

    // Paged user list endpoints
    "GetUserCardPrintError".paged("userCardPrintErrorList") { db.userCardPrintState.findByUser_Card_ExtIdAndHasCompleted(uid, false) }
    "GetUserCharacter".paged("userCharacterList") { db.userCharacter.findByUser_Card_ExtId(uid) }
    "GetUserCourse".paged("userCourseList") { db.userCourse.findByUser_Card_ExtId(uid) }
    "GetUserCharge".paged("userChargeList") { db.userCharge.findByUser_Card_ExtId(uid) }
    "GetUserDuel".paged("userDuelList") { db.userDuel.findByUser_Card_ExtId(uid) }
    "GetUserGacha".paged("userGachaList") { db.userGacha.findByUser_Card_ExtId(uid) }

    // Paged user list endpoints that has a kind in their request
    "GetUserActivity".pagedWithKind("userActivityList") {
        val kind = parsing { data["kind"]!!.int }
        mapOf("kind" to kind) grabs {
            db.userActivity.findAllByUser_Card_ExtIdAndKind(uid, kind).sortedBy { -it.sortNumber }
        }
    }

    // Check dev/chusan_dev_notes for more item information
    val penguins = ls(8000, 8010, 8020, 8030)

    "GetUserItem".pagedWithKind("userItemList") {
        val rawIndex = data["nextIndex"]!!.long
        val kind = parsing { (rawIndex / 10000000000L).int }
        data["nextIndex"] = rawIndex % 10000000000L
        mapOf("itemKind" to kind) grabs {
            // TODO: All unlock
            val items = db.userItem.findAllByUser_Card_ExtIdAndItemKind(uid, kind).toMutableList()

            // Check game options
            db.userData.findByCard_ExtId(uid)()?.card?.aquaUser?.gameOptions?.let {
                if (it.chusanInfinitePenguins && kind == 5) {
                    items.removeAll { it.itemId in penguins }
                    items.addAll(penguins.map { UserItem(kind, it, 999, true) })
                }
            }

            items
        } postProcess {
            val ni = it["nextIndex"]!!.long
            if (ni != -1L) it["nextIndex"] = ni + (kind * 10000000000L)
        }
    }

    "GetUserFavoriteItem".pagedWithKind("userFavoriteItemList") {
        val kind = parsing { data["kind"]!!.int }
        mapOf("kind" to kind) grabs {
            // TODO: Actually store this info at UpsertUserAll
            val fav = when (kind) {
                1 -> "favorite_music"
                3 -> "favorite_chara"
                else -> null
            }?.let { db.userGeneralData.findByUser_Card_ExtIdAndPropertyKey(uid, it)() }?.propertyValue

            fav?.ifBlank { null }?.split(",")?.map { it.int } ?: emptyList()
        }
    }

    val userPreviewKeys = ("userName,reincarnationNum,level,exp,playerRating,lastGameId,lastRomVersion," +
        "lastDataVersion,trophyId,classEmblemMedal,classEmblemBase,battleRankId").split(',').toSet()

    "GetUserPreview" api@ {
        val user = db.userData.findByCard_ExtId(uid)() ?: return@api null
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

    "GetUserMusic".paged("userMusicList") {
        // Compatibility: Older chusan uses boolean for isSuccess
        fun checkAncient(d: List<UserMusicDetail>) =
            data["version"]?.double?.let { if (it >= 2.15) d else d.map {
                d.toJson().jsonMap().toMutableMap().apply { this["isSuccess"] = this["isSuccess"].truthy }
            } } ?: d

        db.userMusicDetail.findByUser_Card_ExtId(uid).groupBy { it.musicId }
            .mapValues { mapOf("length" to it.value.size, "userMusicDetailList" to checkAncient(it.value)) }
            .values.toList()
    }

    "GetUserLoginBonus".paged("userLoginBonusList") {
        if (!props.loginBonusEnable) empty else db.userLoginBonus.findAllLoginBonus(uid.int, 1, 0)
    }

    "GetUserRecentRating".paged("userRecentRatingList") {
        db.userGeneralData.findByUser_Card_ExtIdAndPropertyKey(uid, "recent_rating_list")()
            ?.propertyValue?.ifBlank { null }
            ?.split(',')?.dropLastWhile { it.isEmpty() }?.map { it.split(':') }
            ?.map { (musicId, level, score) -> UserRecentRating(musicId.int, level.int, "2000001", score.int) }
            ?: listOf()
    }

    "GetUserMapArea" {
        val maps = parsing { data["mapAreaIdList"] as List<Map<String, String>> }
            .mapNotNull { it["mapAreaId"]?.toIntOrNull() }

        mapOf("userId" to uid, "userMapAreaList" to db.userMap.findAllByUserCardExtIdAndMapAreaIdIn(uid, maps))
    }

    "GetUserTeam" {
        val playDate = parsing { data["playDate"] as String }
        val team = db.userData.findByCard_ExtId(uid)()?.card?.aquaUser?.gameOptions?.chusanTeamName?.ifBlank { null }
            ?: props.teamName?.ifBlank { null } ?:  "一緒に歌おう！"

        mapOf(
            "userId" to uid, "teamId" to 1, "teamRank" to 1, "teamName" to team,
            "userTeamPoint" to mapOf("userId" to uid, "teamId" to 1, "orderId" to 1, "teamPoint" to 1, "aggrDate" to playDate)
        )
    }

    // Game settings
    "GetGameSetting" {
        val version = data["version"].toString()

        // Fixed reboot time triggers chusan maintenance lockout, so let's try minime method which sets it dynamically
        // Special thanks to skogaby
        // Hardcode so that the reboot time always started 3 hours ago and ended 2 hours ago
        val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
        val now = jstNow()

        // Set the matching & reflector to the one set in the game options, or the external matching server
        val opts = TokenChecker.getCurrentSession()?.user?.gameOptions
        val matching = opts?.chusanMatchingServer?.ifBlank { null } ?:
            props.externalMatching?.ifBlank { null } ?:
            (req.getHeader("wrapper original url") ?: req.requestURL.toString())
                .removeSuffix("GetGameSettingApi").removeSuffix("ChuniServlet/")
        val reflector = opts?.chusanMatchingReflector?.ifBlank { null } ?:
            props.reflectorUrl

        mapOf(
            "gameSetting" to mapOf(
                "romVersion" to "$version.00",
                "dataVersion" to versionHelper[data["clientId"].toString()],
                "isMaintenance" to false,
                "requestInterval" to 0,
                "rebootStartTime" to now.minusHours(4).format(fmt),
                "rebootEndTime" to now.minusHours(3).format(fmt),
                "isBackgroundDistribute" to false,
                "maxCountCharacter" to 300,
                "maxCountItem" to 300,
                "maxCountMusic" to 300,
                "matchStartTime" to now.minusHours(1).format(fmt),
                "matchEndTime" to now.plusHours(7).format(fmt),
                "matchTimeLimit" to 10,
                "matchErrorLimit" to 10,
                "matchingUri" to matching.ensureEndingSlash(),
                "matchingUriX" to matching.ensureEndingSlash(),
                "udpHolePunchUri" to reflector?.ensureEndingSlash(),
                "reflectorUri" to reflector?.ensureEndingSlash(),
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
    "GetGameMapAreaCondition" static { ChusanData.mapAreaCondition }

    // CardMaker (TODO: Somebody test this, I don't have a card maker)
    "CMGetUserData" {
        val user = db.userData.findByCard_ExtId(uid)() ?: (400 - "User not found")
        user.userEmoney = UserEmoney()
        mapOf("userId" to uid, "userData" to user, "userEmoney" to user.userEmoney)
    }
    "CMGetUserPreview" {
        val user = db.userData.findByCard_ExtId(uid)() ?: (400 - "User not found")
        mapOf("userName" to user.userName, "level" to user.level, "medal" to user.medal, "lastDataVersion" to user.lastDataVersion, "isLogin" to false)
    }
}