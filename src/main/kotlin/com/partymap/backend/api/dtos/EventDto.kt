package com.partymap.backend.api.dtos

import com.partymap.backend.domain.event.db.EventType
import java.time.LocalTime
import java.util.*

data class EventDto(
    val id: UUID,
    val title: String,
    val placeId: UUID,
    val description: String,
    val start: LocalTime,
    val end: LocalTime,
    val image: String?,
    val lineupItems: List<EventLineupItemDto>,
    val price: String?,
    val kind: EventType,
    val links: List<LinkDto>,
)
