@file:Suppress("UNCHECKED_CAST")

package icu.samnyan.aqua.sega.chusan

import ext.*
import icu.samnyan.aqua.sega.chusan.model.response.data.MatchingWaitState
import icu.samnyan.aqua.sega.chusan.model.userdata.Chu3MatchingMemberReq


fun ChusanController.matchingApiInit() {
    if (props.externalMatching.isNullOrBlank()) serverOnlyMatching()
    else if (props.proxiedMatching) proxyMatching()
}

/**
 * Matching implementation that matches you to players in this server only (not tested very well)
 */
fun ChusanController.serverOnlyMatching() {
    // Matching
    data class MatchingRoom(val members: MutableList<Chu3MatchingMemberReq>, val startTime: Long)
    val matchingRooms = mutableMapOf<Int, MatchingRoom>()
    var matchingLast = 0
    val matchingTime = 120  // Seconds

    "BeginMatching" {
        val memberInfo = parsing { mapper.convert<Chu3MatchingMemberReq>(data["matchingMemberInfo"] as JDict) }

        // Check if there are any room available with less than 4 members and not started
        var id = matchingRooms.entries.find { it.value.members.size < 4 && it.value.startTime == 0L }?.key
        if (id == null) {
            matchingLast += 1
            id = matchingLast
            matchingRooms[id] = MatchingRoom(mutableListOf(memberInfo), millis())
        }

        mapOf("roomId" to id, "matchingWaitState" to MatchingWaitState(listOf(memberInfo)))
    }

    "GetMatchingState" api@ {
        val roomId = parsing { data["roomId"]!!.int }
        val room = matchingRooms[roomId] ?: return@api null
        val dt = matchingTime - (millis() - room.startTime) / 1000
        val ended = room.members.size == 4 || dt <= 0

        mapOf("roomId" to roomId, "matchingWaitState" to MatchingWaitState(room.members, ended, dt.int, 1))
    }

    "EndMatching" api@ {
        val roomId = parsing { data["roomId"]!!.int }
        val room = matchingRooms[roomId] ?: return@api null
        mapOf(
            "matchingMemberInfoList" to room.members,
            "matchingMemberRoleList" to room.members.indices.map { mapOf("role" to it) },
            "matchingResult" to 1,
            "reflectorUri" to props.reflectorUrl
        )
    }
}

/**
 * Matching implementation
 */
fun ChusanController.proxyMatching() {
    val ext = props.externalMatching!!

    // ID Cache <obfuscated: original> is used to obfuscate the user ID
    val processedCache = mutableSetOf<Long>()
    val idCache = mutableMapOf<Long, Long>()

    fun Chu3MatchingMemberReq.checkFromAquaDX(): Boolean {
        if (userId in idCache) return true
        if (userId in processedCache) return false

        // Check if this user is from our server
        val user = db.userData.findByCard_ExtId(userId)()
        if (user == null) {
            // User is from another server, check if they have been checked in
            if (db.matchingMember.existsByUserIdAndUserName(userId, userName)) {
                // Check in
                db.matchingMember.save(this)
                log.info("[Matching] User $userId ($userName) not found, checking in.")
            }
            processedCache.add(userId)
        }
        else {
            // Is from our server, obfuscate the user ID to enhance security
            val randomId = (0..Int.MAX_VALUE).random().toLong()
            idCache[randomId] = userId
            userId = randomId
            log.info("[Matching] User $userId ($userName) is from our server, obfuscated to $randomId.")
        }
        return user != null
    }

    "BeginMatching" {
        val member = parsing { mapper.convert<Chu3MatchingMemberReq>(data["matchingMemberInfo"] as JDict) }
        member.checkFromAquaDX()

        // Forward BeginMatching to external server
//        val res =
    }

    TODO("The external matching API is not implemented yet.")
}
