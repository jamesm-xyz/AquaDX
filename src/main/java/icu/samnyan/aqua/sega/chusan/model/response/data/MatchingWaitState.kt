package icu.samnyan.aqua.sega.chusan.model.response.data

import com.fasterxml.jackson.annotation.JsonProperty

class MatchingWaitState(
    var matchingMemberInfoList: List<MatchingMemberInfo> = listOf(),

    @JsonProperty("isFinish")
    var isFinish: Boolean = false,
    var restMSec: Int = 30000,
    var pollingInterval: Int = 10,
)
