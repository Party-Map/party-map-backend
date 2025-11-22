package com.partymap.backend.api.mappers

import com.partymap.backend.api.dtos.UpcomingEventForPlaceDto
import com.partymap.backend.domain.event.db.EventEntity

fun EventEntity.toUpcomingEventDto(): UpcomingEventForPlaceDto {
    val fallbackImage = this.place.image
    val finalImage = this.image ?: fallbackImage

    return UpcomingEventForPlaceDto(
        id = id!!,
        title = title,
        image = finalImage,
        start = this.start,
        kind = this.kindTag.toDto(),
    )
}