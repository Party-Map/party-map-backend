package com.partymap.backend.domain.event.dto

import java.time.LocalDateTime
import java.util.*

data class EventAdminListItemDto(
    val id: UUID,
    val title: String,
    var start: LocalDateTime,
    var end: LocalDateTime,
    val placeName: String,
)
