package com.partymap.backend.domain.eventplan.dto

import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.performer.dto.PerformerDto
import java.time.LocalDateTime
import java.util.*

data class EventPlanLineupInvitationForPerformerDto(
    val eventPlanId: UUID,
    val eventPlanTitle: String,
    val state: EventPlanLineupInvitationState,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    val performer: PerformerDto,
)
