package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.place.dto.PlaceDto
import java.time.LocalDateTime
import java.util.*

data class EventPlanPlaceInvitationDto(
    val state: EventPlanPlaceInvitationState,
    val place: PlaceDto,
)

data class EventPlanPlaceInvitationWithDateDto(
    val eventPlanId: UUID,
    val state: EventPlanPlaceInvitationState,
    val title: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
)
