package com.partymap.backend.api.mappers

import com.partymap.backend.api.dtos.*
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.user.db.UserEntity

fun PerformerEntity.toDto(): PerformerDto =
    PerformerDto(
        id = id!!,
        name = name,
        genre = genre,
        bio = bio,
        image = image,
        links = links.map { LinkDto(it.type, it.url) },
    )

fun PerformerEntity.toSearchHitDto(): SearchHitDto {
    return SearchHitDto(
        id = this.id!!,
        type = SearchHitType.PERFORMER,
        title = this.name,
        subtitle = this.genre,
        image = this.image,
        nextEventStart = null,
        placeId = null,
    )
}

fun PerformerEntity.toAdminListItemDto(): PerformerAdminListItemDto =
    PerformerAdminListItemDto(
        id = this.id!!,
        name = this.name,
    )

fun PerformerCreateDto.toEntity(owner: UserEntity): PerformerEntity =
    PerformerEntity(
        name = name,
        genre = genre,
        bio = bio,
        image = image,
        links = (links ?: emptyList()).map {
            LinkEmbeddable(
                type = it.type,
                url = it.url,
            )
        }.toMutableList(),
        owner = owner,
    )

fun PerformerEntity.updateFromDto(dto: PerformerCreateDto) {
    name = dto.name
    genre = genre
    bio = bio
    image = image
    links = (dto.links ?: emptyList()).map {
        LinkEmbeddable(
            type = it.type,
            url = it.url,
        )
    }.toMutableList()
}