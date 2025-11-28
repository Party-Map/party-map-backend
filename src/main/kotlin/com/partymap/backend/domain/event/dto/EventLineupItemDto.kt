package com.partymap.backend.domain.event.dto

import com.partymap.backend.domain.performer.dto.PerformerDto
import java.time.LocalTime

data class EventLineupItemDto(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val performer: PerformerDto
)
