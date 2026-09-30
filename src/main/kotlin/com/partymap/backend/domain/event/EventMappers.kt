package com.partymap.backend.domain.event

import com.partymap.backend.domain.common.dto.LinkDto
import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventLineupItemEntity
import com.partymap.backend.domain.event.dto.EventAdminListItemDto
import com.partymap.backend.domain.event.dto.EventDto
import com.partymap.backend.domain.event.dto.EventLineupItemDto
import com.partymap.backend.domain.event.dto.PlaceUpcomingEventDto
import com.partymap.backend.domain.performer.toDto
import com.partymap.backend.domain.search.SearchHitDto
import com.partymap.backend.domain.search.SearchHitType

fun EventEntity.toDto(): EventDto = EventDto(
    id = id!!,
    title = title,
    placeId = place.id!!,
    description = description,
    start = start,
    end = end,
    image = image,
    lineupItems = lineupItems.map { it.toDto() },
    price = price,
    kind = kind,
    links = links.map { LinkDto(it.type, it.url) },
)

fun EventEntity.toPlaceUpcomingEventDto(): PlaceUpcomingEventDto {
    val fallbackImage = this.place.image
    val finalImage = this.image ?: fallbackImage

    return PlaceUpcomingEventDto(
        placeId = place.id!!,
        eventId = id!!,
        title = title,
        image = finalImage,
        start = this.start,
        kind = this.kind,
    )
}

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

fun EventEntity.toAdminListItemDto(): EventAdminListItemDto = EventAdminListItemDto(
    id = id!!,
    title = title,
    start = start,
    end = end,
    placeName = place.name,
)

fun EventLineupItemEntity.toDto(): EventLineupItemDto = EventLineupItemDto(
    startTime = startTime,
    endTime = endTime,
    performer = id.performer.toDto(),
)
