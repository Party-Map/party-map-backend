package com.partymap.backend.api.mappers

import com.partymap.backend.api.dtos.LinkDto
import com.partymap.backend.api.dtos.PerformerDto
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
