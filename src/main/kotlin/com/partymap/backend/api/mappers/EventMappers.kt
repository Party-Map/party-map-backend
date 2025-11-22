package com.partymap.backend.api.mappers

import com.partymap.backend.api.dtos.EventDto
import com.partymap.backend.api.dtos.LinkDto
import com.partymap.backend.api.dtos.SearchHitDto
import com.partymap.backend.api.dtos.SearchHitType
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
        kind = kind,
        links = links.map { LinkDto(it.type, it.url) },
    )

fun EventEntity.toSearchHitDto(): SearchHitDto {
    val place = this.place

    val subtitle = buildString {
        append(place.name)
        append(" • ")
        append(place.city)
    }

    val image = this.image ?: place.image

    return SearchHitDto(
        id = this.id!!,
        type = SearchHitType.EVENT,
        title = this.title,
        subtitle = subtitle,
        image = image,
        nextEventStart = this.start,
        placeId = place.id,
    )
}