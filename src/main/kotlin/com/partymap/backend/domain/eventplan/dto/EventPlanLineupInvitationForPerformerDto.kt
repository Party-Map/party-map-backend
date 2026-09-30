package com.partymap.backend.domain.eventplan.dto

import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.performer.dto.PerformerDto
import java.time.LocalDateTime
import java.util.UUID

data class EventPlanLineupInvitationForPerformerDto(
    val eventPlanId: UUID,
    val eventPlanTitle: String,
    val state: EventPlanLineupInvitationState,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val performer: PerformerDto,
)
