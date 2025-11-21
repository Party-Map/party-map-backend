package com.partymap.backend.api.mappers


import com.partymap.backend.api.dtos.GeoPointDto
import com.partymap.backend.api.dtos.LinkDto
import com.partymap.backend.api.dtos.PlaceDto
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
