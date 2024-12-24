package icu.samnyan.aqua.net.games.chu3

import ext.*
import icu.samnyan.aqua.net.db.AquaUserServices
import icu.samnyan.aqua.net.games.*
import icu.samnyan.aqua.net.utils.*
import icu.samnyan.aqua.sega.chusan.model.*
import icu.samnyan.aqua.sega.chusan.model.userdata.Chu3UserData
import org.springframework.web.bind.annotation.RestController

@RestController
@API("api/v2/game/chu3")
class Chusan(
    override val us: AquaUserServices,
    override val playlogRepo: Chu3UserPlaylogRepo,
    override val userDataRepo: Chu3UserDataRepo,
    val rp: Chu3Repos
): GameApiController<Chu3UserData>("chu3", Chu3UserData::class) {
    override suspend fun trend(@RP username: Str): List<TrendOut> = us.cardByName(username) { card ->
        findTrend(playlogRepo.findByUserCardExtId(card.extId)
            .map { TrendLog(it.playDate.toString(), it.playerRating) })
    }

    // Only show > AAA rank
    override val shownRanks = chu3Scores.filter { it.first >= 95 * 10000 }
    override val settableFields: Map<String, (Chu3UserData, String) -> Unit> by lazy { mapOf(
        "userName" to usernameCheck(SEGA_USERNAME_CAHRS),
        "nameplateId" to { u, v -> u.nameplateId = v.int },
        "frameId" to { u, v -> u.frameId = v.int },
        "trophyId" to { u, v -> u.trophyId = v.int },
        "mapIconId" to { u, v -> u.mapIconId = v.int },
        "voiceId" to { u, v -> u.voiceId = v.int },
        "avatarItem" to { u, v -> v.split(':', limit=2).map { it.int }.let { (cat, data) -> when (cat) {
            1 -> u.avatarWear = data
            2 -> u.avatarHead = data
            3 -> u.avatarFace = data
            4 -> u.avatarSkin = data
            5 -> u.avatarItem = data
            6 -> u.avatarFront = data
            7 -> u.avatarBack = data
        } } }
    ) }

    override suspend fun userSummary(@RP username: Str, @RP token: String?) = us.cardByName(username) { card ->
        // Summary values: total plays, player rating, server-wide ranking
        // number of each rank, max combo, number of full combo, number of all perfect
        val extra = rp.userGeneralData.findByUser_Card_ExtId(card.extId)
            .associate { it.propertyKey to it.propertyValue }

        val ratingComposition = mapOf(
            "recent" to (extra["recent_rating_list"] ?: ""),
        )

        genericUserSummary(card, ratingComposition)
    }

    // UserBox related APIs
    @API("user-box")
    fun userBox(@RP token: String) = us.jwt.auth(token) {
        userDataRepo.findByCard(it.ghostCard) ?: (404 - "Game data not found") }

    @API("user-box-item-by-kind")
    fun userBoxItem(@RP token: String, @RP itemId: Int) = us.jwt.auth(token) {
        rp.userItem.findAllByUser_Card_ExtIdAndItemKind(it.ghostCard.extId, itemId) }

    @API("user-box-all-items")
    fun userBoxAllItems() = allItems
    val allItems by lazy { mapOf(
        "nameplate" to rp.gameNamePlate.findAll(),
        "frame" to rp.gameFrame.findAll(),
        "trophy" to rp.gameTrophy.findAll(),
        "mapicon" to rp.gameMapIcon.findAll(),
        "sysvoice" to rp.gameSystemVoice.findAll(),
        "avatar" to rp.gameAvatarAcc.findAll(),
    ) }
}
