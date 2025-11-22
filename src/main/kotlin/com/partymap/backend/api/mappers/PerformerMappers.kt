package com.partymap.backend.api.mappers

import com.partymap.backend.api.dtos.LinkDto
import com.partymap.backend.api.dtos.PerformerDto
import com.partymap.backend.domain.performer.db.PerformerEntity

fun PerformerEntity.toDto(): PerformerDto =
    PerformerDto(
        id = id!!,
        name = name,
        genreTag = genreTag?.toDto(),
        bio = bio,
        image = image,
        tags = tags.map { it.toDto() },
        links = links.map { LinkDto(it.type, it.url) },
    )
