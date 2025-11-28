package com.partymap.backend.api.dtos

import java.time.LocalTime

data class EventLineupItemDto(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val performer: PerformerDto
)
