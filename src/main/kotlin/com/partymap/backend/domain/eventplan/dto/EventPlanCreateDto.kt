package com.partymap.backend.domain.eventplan.dto

import com.partymap.backend.domain.common.dto.LinkDto
import com.partymap.backend.domain.event.db.EventType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/** Body of the create and update requests; the end must be after the start (checked by the service). */
data class EventPlanCreateDto(
    @field:NotBlank
    @field:Size(max = 255)
    val title: String,
    val description: String = "",
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    @field:Size(max = 2048)
    val image: String? = null,
    @field:Size(max = 255)
    val price: String? = null,
    val kind: EventType,
    @field:Valid
    val links: List<LinkDto>? = null,
)
