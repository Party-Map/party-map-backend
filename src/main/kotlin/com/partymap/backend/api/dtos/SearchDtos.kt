package com.partymap.backend.api.dtos

import java.time.Instant
import java.util.UUID

enum class SearchHitType {
    PLACE,
    EVENT,
    PERFORMER,
}

data class SearchHitDto(
    val id: UUID,
    val type: SearchHitType,

    val title: String,
    val subtitle: String,
    val image: String?,

    val nextEventStart: Instant?,

    val placeId: UUID?,
)
data class SearchResponseDto(
    val query: String,
    val hits: List<SearchHitDto>,
)