package com.partymap.backend.domain.eventplan.dto

import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.performer.dto.PerformerDto
import java.time.LocalDateTime

data class EventPlanLineupInvitationDto(
    val state: EventPlanLineupInvitationState,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    val performer: PerformerDto,
)
