package com.partymap.backend.api.dtos

import com.partymap.backend.domain.event.db.EventType
import java.time.Instant
import java.util.*

data class EventDto(
    val id: UUID,
    val title: String,
    val placeId: UUID,
    val description: String,
    val start: Instant,       // ISO-8601
    val end: Instant,
    val image: String?,
    val performerIds: List<UUID>,
    val price: String?,
    val kind: EventType,
    val links: List<LinkDto>,
)
