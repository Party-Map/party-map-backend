package com.partymap.backend.api.dtos


data class LikedEventsGroupedDto(
    val upcoming: List<EventDto> = emptyList(),
    val past: List<EventDto> = emptyList(),
)
