package com.partymap.backend.domain.eventplan.dto

import java.time.LocalDateTime
import java.util.UUID

data class EventPlanAdminListItemDto(
    val id: UUID,
    val title: String,
    var startDateTime: LocalDateTime,
    var endDateTime: LocalDateTime,
)
