package com.partymap.backend.api.dtos

import java.time.Instant
import java.util.*

data class UpcomingEventForPlaceDto(
    val id: UUID,
    val title: String,
    val image: String?,
    val start: Instant,
    val kind: TagDto,
)