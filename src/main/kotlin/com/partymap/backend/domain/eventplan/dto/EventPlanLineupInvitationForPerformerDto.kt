package com.partymap.backend.domain.eventplan.dto

import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.performer.dto.PerformerDto
import java.time.LocalTime
import java.util.*

data class EventPlanLineupInvitationForPerformerDto(
    val eventPlanId: UUID,
    val eventPlanTitle: String,
    val state: EventPlanLineupInvitationState,
    var startTime: LocalTime,
    var endTime: LocalTime,
    val performer: PerformerDto,
)
