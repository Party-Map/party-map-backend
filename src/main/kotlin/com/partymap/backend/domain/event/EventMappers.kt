package com.partymap.backend.domain.event

import com.partymap.backend.domain.common.dto.toDto
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
    id = requiredId,
    title = title,
    placeId = place.requiredId,
    description = description,
    start = start,
    end = end,
    image = image,
    lineupItems = lineupItems.map { it.toDto() },
    price = price,
    kind = kind,
    links = links.map { it.toDto() },
)

/** The event as the map popup shows it; the place's image stands in for a missing event image. */
fun EventEntity.toPlaceUpcomingEventDto(): PlaceUpcomingEventDto = PlaceUpcomingEventDto(
    placeId = place.requiredId,
    eventId = requiredId,
    title = title,
    image = image ?: place.image,
    start = start,
    kind = kind,
)

fun EventEntity.toSearchHitDto(): SearchHitDto = SearchHitDto(
    id = requiredId,
    type = SearchHitType.EVENT,
    title = title,
    subtitle = "${place.name} • ${place.city}",
    image = image ?: place.image,
    nextEventStart = start,
    placeId = place.requiredId,
)

fun EventEntity.toAdminListItemDto(): EventAdminListItemDto = EventAdminListItemDto(
    id = requiredId,
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
