package com.partymap.backend.api.dtos

import java.time.Instant
import java.util.*

data class EventAdminListItemDto(
    val id: UUID,
    val title: String,
    var start: Instant,
    var end: Instant,
    val placeName: String,
)
