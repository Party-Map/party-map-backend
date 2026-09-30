package com.partymap.backend.domain.eventplan.dto

import java.time.LocalDateTime
import java.util.UUID

data class EventPlanLineupInvitationCreatePayloadDto(
    val performerId: UUID,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
