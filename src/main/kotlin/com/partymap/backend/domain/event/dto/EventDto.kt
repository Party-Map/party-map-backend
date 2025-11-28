package com.partymap.backend.domain.event.dto

import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.like.dto.LinkDto
import java.time.LocalDateTime
import java.util.*

data class EventDto(
    val id: UUID,
    val title: String,
    val placeId: UUID,
    val description: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val image: String?,
    val lineupItems: List<EventLineupItemDto>,
    val price: String?,
    val kind: EventType,
    val links: List<LinkDto>,
)
