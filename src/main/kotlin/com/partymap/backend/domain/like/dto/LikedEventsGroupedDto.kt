package com.partymap.backend.domain.like.dto

import com.partymap.backend.domain.event.dto.EventDto

data class LikedEventsGroupedDto(val upcoming: List<EventDto> = emptyList(), val past: List<EventDto> = emptyList())
