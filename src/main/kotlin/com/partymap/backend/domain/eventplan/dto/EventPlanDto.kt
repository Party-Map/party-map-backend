package com.partymap.backend.domain.eventplan.dto

import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.like.dto.LinkDto
import java.time.LocalDateTime
import java.util.*

data class EventPlanDto(
    val id: UUID,
    val title: String,
    val description: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val image: String?,
    val price: String?,
    val kind: EventType,
    val links: List<LinkDto>,
    val placeInvitation: EventPlanPlaceInvitationDto?,
    val lineupInvitations: List<EventPlanLineupInvitationDto>,
)