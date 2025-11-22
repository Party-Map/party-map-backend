package com.partymap.backend.api.mappers


import com.partymap.backend.api.dtos.GeoPointDto
import com.partymap.backend.api.dtos.LinkDto
import com.partymap.backend.api.dtos.PlaceDto
import com.partymap.backend.api.dtos.SearchHitDto
import com.partymap.backend.api.dtos.SearchHitType
import com.partymap.backend.domain.place.db.PlaceEntity

fun PlaceEntity.toDto(): PlaceDto =
    PlaceDto(
        id = id!!,
        name = name,
        location = GeoPointDto(
            latitude = location.latitude,
            longitude = location.longitude,
        ),
        address = address,
        city = city,
        description = description,
        image = image,
        tags = tags.toList(),
        links = links.map { LinkDto(it.type, it.url) },
    )

fun PlaceEntity.toSearchHitDto(): SearchHitDto {
    val subtitle = buildString {
        append(city)
        if (address.isNotBlank()) {
            append(" • ")
            append(address)
        }
    }

    return SearchHitDto(
        id = this.id!!,
        type = SearchHitType.PLACE,
        title = this.name,
        subtitle = subtitle,
        image = this.image,
        nextEventStart = null,
        placeId = this.id,
    )
}