package com.partymap.backend.domain.event.dto

import java.time.LocalTime
import java.util.*

data class EventAdminListItemDto(
    val id: UUID,
    val title: String,
    var start: LocalTime,
    var end: LocalTime,
    val placeName: String,
)
