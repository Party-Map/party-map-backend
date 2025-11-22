package com.partymap.backend.api.mappers

import com.partymap.backend.api.dtos.LinkDto
import com.partymap.backend.api.dtos.PerformerDto
import com.partymap.backend.api.dtos.SearchHitDto
import com.partymap.backend.api.dtos.SearchHitType
import com.partymap.backend.domain.performer.db.PerformerEntity

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