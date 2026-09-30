package com.partymap.backend.domain.performer

import com.partymap.backend.domain.common.dto.toDto
import com.partymap.backend.domain.common.dto.toEmbeddables
import com.partymap.backend.domain.common.trimToNull
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.performer.dto.PerformerAdminListItemDto
import com.partymap.backend.domain.performer.dto.PerformerCreateDto
import com.partymap.backend.domain.performer.dto.PerformerDto
import com.partymap.backend.domain.search.SearchHitDto
import com.partymap.backend.domain.search.SearchHitType
import com.partymap.backend.domain.user.UserEntity

fun PerformerEntity.toDto(): PerformerDto = PerformerDto(
    id = requiredId,
    name = name,
    genre = genre,
    bio = bio,
    image = image,
    links = links.map { it.toDto() },
)

fun PerformerEntity.toSearchHitDto(): SearchHitDto = SearchHitDto(
    id = requiredId,
    type = SearchHitType.PERFORMER,
    title = name,
    subtitle = genre,
    image = image,
    nextEventStart = null,
    placeId = null,
)

fun PerformerEntity.toAdminListItemDto(): PerformerAdminListItemDto = PerformerAdminListItemDto(
    id = requiredId,
    name = name,
)

fun PerformerCreateDto.toEntity(owner: UserEntity): PerformerEntity = PerformerEntity(
    name = name.trim(),
    genre = genre.trim(),
    bio = bio,
    image = image.trimToNull(),
    links = links.toEmbeddables(),
    owner = owner,
)

fun PerformerEntity.updateFromDto(dto: PerformerCreateDto) {
    name = dto.name.trim()
    genre = dto.genre.trim()
    bio = dto.bio
    image = dto.image.trimToNull()
    links.clear()
    links.addAll(dto.links.toEmbeddables())
}
