package com.partymap.backend.api.dtos

import com.partymap.backend.domain.event.db.EventType
import java.time.Instant
import java.util.UUID

data class UpcomingEventForPlaceDto (
    val id: UUID?,
    val title: String,
    val image: String?,
    val start: Instant,
    val kind: EventType,
)