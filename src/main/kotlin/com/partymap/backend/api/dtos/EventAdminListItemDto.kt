package com.partymap.backend.api.dtos

import java.time.LocalTime
import java.util.*

data class EventAdminListItemDto(
    val id: UUID,
    val title: String,
    var start: LocalTime,
    var end: LocalTime,
    val placeName: String,
)
