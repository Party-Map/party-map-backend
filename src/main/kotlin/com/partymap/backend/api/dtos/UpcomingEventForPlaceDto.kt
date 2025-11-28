package com.partymap.backend.api.dtos

import com.partymap.backend.domain.event.db.EventType
import java.time.LocalTime
import java.util.*

data class PlaceUpcomingEventDto(
    val placeId: UUID,
    val eventId: UUID,
    val title: String,
    val image: String?,
    val start: LocalTime,
    val kind: EventType,
)
