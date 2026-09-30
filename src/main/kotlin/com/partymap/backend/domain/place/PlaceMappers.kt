package com.partymap.backend.domain.place

import com.partymap.backend.domain.common.db.GeoPointEmbeddable
import com.partymap.backend.domain.common.dto.GeoPointDto
import com.partymap.backend.domain.common.dto.toDto
import com.partymap.backend.domain.common.dto.toEmbeddables
import com.partymap.backend.domain.common.trimToNull
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.place.dto.PlaceAdminListItemDto
import com.partymap.backend.domain.place.dto.PlaceCreateDto
import com.partymap.backend.domain.place.dto.PlaceDto
import com.partymap.backend.domain.search.SearchHitDto
import com.partymap.backend.domain.search.SearchHitType
import com.partymap.backend.domain.user.UserEntity

fun PlaceEntity.toDto(): PlaceDto = PlaceDto(
    id = requiredId,
    name = name,
    location = GeoPointDto(location.latitude, location.longitude),
    address = address,
    city = city,
    description = description,
    image = image,
    tags = tags.sorted(),
    links = links.map { it.toDto() },
)

fun PlaceEntity.toSearchHitDto(): SearchHitDto = SearchHitDto(
    id = requiredId,
    type = SearchHitType.PLACE,
    title = name,
    subtitle = if (address.isBlank()) city else "$city • $address",
    image = image,
    nextEventStart = null,
    placeId = requiredId,
)

fun PlaceEntity.toAdminListItemDto(): PlaceAdminListItemDto = PlaceAdminListItemDto(
    id = requiredId,
    name = name,
    address = address,
    city = city,
)

fun PlaceCreateDto.toEntity(owner: UserEntity): PlaceEntity = PlaceEntity(
    name = name.trim(),
    location = GeoPointEmbeddable(location.latitude, location.longitude),
    address = address.trim(),
    city = city.trim(),
    description = description,
    image = image.trimToNull(),
    tags = cleanTags(),
    links = links.toEmbeddables(),
    owner = owner,
)

fun PlaceEntity.updateFromDto(dto: PlaceCreateDto) {
    name = dto.name.trim()
    location = GeoPointEmbeddable(dto.location.latitude, dto.location.longitude)
    address = dto.address.trim()
    city = dto.city.trim()
    description = dto.description
    image = dto.image.trimToNull()
    tags.clear()
    tags.addAll(dto.cleanTags())
    links.clear()
    links.addAll(dto.links.toEmbeddables())
}

private fun PlaceCreateDto.cleanTags(): MutableSet<String> =
    tags.orEmpty().map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
