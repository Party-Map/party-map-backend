package com.partymap.backend.api.mappers


import com.partymap.backend.api.dtos.GeoPointDto
import com.partymap.backend.api.dtos.LinkDto
import com.partymap.backend.api.dtos.PlaceAdminListItemDto
import com.partymap.backend.api.dtos.PlaceCreateDto
import com.partymap.backend.api.dtos.PlaceDto
import com.partymap.backend.api.dtos.SearchHitDto
import com.partymap.backend.api.dtos.SearchHitType
import com.partymap.backend.domain.common.db.GeoPointEmbeddable
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.user.db.UserEntity

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
fun GeoPointDto.toEmbeddable() =
    GeoPointEmbeddable(latitude = latitude, longitude = longitude)


fun PlaceCreateDto.toEntity(owner: UserEntity): PlaceEntity =
    PlaceEntity(
        name = name,
        location = location.toEmbeddable(),
        address = address,
        city = city,
        description = description,
        image = image,
        tags = (tags ?: emptyList()).toMutableSet(),
        links = (links ?: emptyList()).map {
            LinkEmbeddable(
                type = it.type,
                url = it.url,
            )
        }.toMutableList(),
        owner = owner,
    )

fun PlaceEntity.toAdminListItemDto(): PlaceAdminListItemDto =
    PlaceAdminListItemDto(
        id = this.id!!,
        name = this.name,
        address = this.address,
        city = this.city,
    )

fun PlaceEntity.updateFromDto(dto: PlaceCreateDto) {
    name = dto.name
    location = dto.location.toEmbeddable()
    address = dto.address
    city = dto.city
    description = dto.description
    image = dto.image
    tags.clear()
    tags.addAll(dto.tags ?: emptyList())
    links = (dto.links ?: emptyList()).map {
        LinkEmbeddable(
            type = it.type,
            url = it.url,
        )
    }.toMutableList()
}