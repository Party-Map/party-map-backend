package com.partymap.backend.domain.event.dto

import com.partymap.backend.domain.performer.dto.PerformerDto
import java.time.LocalDateTime

data class EventLineupItemDto(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val performer: PerformerDto
)
