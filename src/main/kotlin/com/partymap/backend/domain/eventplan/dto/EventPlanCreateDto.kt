package com.partymap.backend.domain.eventplan.dto

import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.like.dto.LinkDto
import java.time.LocalDateTime

data class EventPlanCreateDto(
    val title: String,
    val description: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val image: String? = null,
    val price: String?,
    val kind: EventType,
    val links: List<LinkDto>? = null,
)
