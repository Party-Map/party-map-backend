package com.partymap.backend.domain.performer

import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.like.dto.LinkDto
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.performer.dto.PerformerAdminListItemDto
import com.partymap.backend.domain.performer.dto.PerformerCreateDto
import com.partymap.backend.domain.performer.dto.PerformerDto
import com.partymap.backend.domain.search.SearchHitDto
import com.partymap.backend.domain.search.SearchHitType
import com.partymap.backend.domain.user.UserEntity

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
    genre = dto.genre
    bio = dto.bio
    image = dto.image
    links = (dto.links ?: emptyList()).map {
        LinkEmbeddable(
            type = it.type,
            url = it.url,
        )
    }.toMutableList()
}