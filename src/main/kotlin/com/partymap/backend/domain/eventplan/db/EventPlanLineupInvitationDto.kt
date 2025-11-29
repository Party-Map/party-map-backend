package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.performer.dto.PerformerDto
import java.time.LocalTime

data class EventPlanLineupInvitationDto(
    val state: EventPlanLineupInvitationState,
    var startTime: LocalTime,
    var endTime: LocalTime,
    val performer: PerformerDto,
)
