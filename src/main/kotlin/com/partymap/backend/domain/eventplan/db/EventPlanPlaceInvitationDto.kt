package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.place.dto.PlaceDto

data class EventPlanPlaceInvitationDto(
    val state: EventPlanPlaceInvitationState,
    val place: PlaceDto,
)
