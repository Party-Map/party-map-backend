package com.partymap.backend.domain.eventplan.dto

import java.time.LocalDateTime
import java.util.UUID

data class EventPlanLineupInvitationCreatePayloadDto(
    var performerId: UUID,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
)
