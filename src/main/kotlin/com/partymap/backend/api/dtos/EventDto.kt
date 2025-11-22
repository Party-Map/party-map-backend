package com.partymap.backend.api.dtos

import java.time.Instant
import java.util.UUID

data class EventDto(
    val id: UUID,
    val title: String,
    val placeId: UUID,
    val description: String,
    val start: Instant,       // ISO-8601
    val end: Instant,
    val image: String?,
    val performerIds: List<UUID>?,
    val price: String?,
    val kindTag: TagDto,
    val tags: List<TagDto>?,
    val links: List<LinkDto>?,
)
