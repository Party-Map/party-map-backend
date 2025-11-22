package com.partymap.backend.api.mappers

import com.partymap.backend.api.dtos.EventDto
import com.partymap.backend.api.dtos.LinkDto
import com.partymap.backend.domain.event.db.EventEntity

fun EventEntity.toDto(): EventDto =
    EventDto(
        id = id!!,
        title = title,
        placeId = place.id!!,
        description = description,
        start = start,
        end = end,
        image = image,
        performerIds = performers.mapNotNull { it.id },
        price = price,
        kindTag = kindTag.toDto(),
        tags = tags.map { it.toDto() },
        links = links.map { LinkDto(it.type, it.url) },
    )
