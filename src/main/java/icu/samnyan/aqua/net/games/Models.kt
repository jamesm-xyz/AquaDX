package icu.samnyan.aqua.net.games

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import ext.JACKSON
import ext.JavaSerializable
import icu.samnyan.aqua.sega.general.model.Card
import icu.samnyan.aqua.sega.util.jackson.AccessCodeSerializer
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.NoRepositoryBean
import java.util.*

data class TrendOut(val date: String, val rating: Int, val plays: Int)

data class RankCount(val name: String, val count: Int)

data class GenericGameSummary(
    val name: String,

    val aquaUser: Map<String, Any?>?,

    val serverRank: Long,
    val accuracy: Double,
    val rating: Int,
    val ratingHighest: Int,
    val ranks: List<RankCount>,
    val detailedRanks: Map<Int, Map<String, Int>>,
    val maxCombo: Int,
    val fullCombo: Int,
    val allPerfect: Int,
    val totalScore: Long,

    val plays: Int,
    val totalPlayTime: Long,
    val joined: String,
    val lastSeen: String,
    val lastVersion: String,
    val lastPlayedHost: String? = null,

    val ratingComposition: Map<String, Any>,

    val recent: List<IGenericGamePlaylog>
)

data class GenericRankingPlayer(
    val rank: Int,
    val name: String,
    val username: String?,
    val accuracy: Double,
    val rating: Int,
    val allPerfect: Int,
    val fullCombo: Int,
    val lastSeen: String
)

@Serializable
data class GenericMusicMeta(
    val name: String?,
    val ver: String,
    val notes: List<GenericNoteMeta>
)

@Serializable
data class GenericNoteMeta(
    val lv: Double?,
)

@Serializable
data class GenericItemMeta(
    val name: String? = null,
    val disable: Boolean? = null,
    val ver: String? = null
)

// Here are some interfaces to generalize across multiple games
interface IUserData {
    val id: Long
    var userName: String
    val playerRating: Int
    val highestRating: Int
    val firstPlayDate: Any
    val lastPlayDate: Any
    val lastRomVersion: String
    val totalScore: Long
    var card: Card?
    val lastClientId: String
}

interface IGenericGamePlaylog {
    val musicId: Int
    val level: Int
    val userPlayDate: Any
    val achievement: Int
    val maxCombo: Int
    val isFullCombo: Boolean
    val beforeRating: Int
    val afterRating: Int
    val isAllPerfect: Boolean
}

@MappedSuperclass
open class BaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    open var id: Long = 0
) : JavaSerializable {
    override fun toString() = JACKSON.writeValueAsString(this)
}

@MappedSuperclass
open class UserDataEntity : BaseEntity() {
    @JsonSerialize(using = AccessCodeSerializer::class)
    @JsonProperty(value = "accessCode", access = JsonProperty.Access.READ_ONLY)
    @OneToOne
    @JoinColumn(name = "aime_card_id", unique = true)
    var card: Card? = null
}

@NoRepositoryBean
interface GenericUserDataRepo<T : IUserData> : JpaRepository<T, Long> {
    fun findByCard(card: Card): T?
    fun findByCard_ExtId(extId: Long): Optional<T>
    @Query("select count(*) from #{#entityName} e where e.playerRating > :rating and e.card.rankingBanned = false")
    fun getRanking(rating: Int): Long
}

@NoRepositoryBean
interface GenericPlaylogRepo<T: IGenericGamePlaylog> : JpaRepository<T, Long> {
    fun findByUserCardExtId(extId: Long): List<T>
    fun findByUserCardExtId(extId: Long, page: Pageable): Page<T>
}

data class ImportResult(val errors: List<String>, val warnings: List<String>, val json: String)
