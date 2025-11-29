package com.partymap.backend.domain.eventplan.db

import java.time.LocalDateTime
import java.util.*

data class EventPlanAdminListItemDto(
    val id: UUID,
    val title: String,
    var startDateTime: LocalDateTime,
    var endDateTime: LocalDateTime,
)