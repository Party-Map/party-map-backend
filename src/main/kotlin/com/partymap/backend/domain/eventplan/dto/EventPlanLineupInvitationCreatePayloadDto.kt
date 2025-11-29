package com.partymap.backend.domain.eventplan.dto


import java.time.LocalTime
import java.util.*

data class EventPlanLineupInvitationCreatePayloadDto(
    var performerId: UUID,
    var startTime: LocalTime,
    var endTime: LocalTime,
)
