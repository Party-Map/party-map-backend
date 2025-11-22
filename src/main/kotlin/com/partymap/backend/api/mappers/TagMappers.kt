package com.partymap.backend.api.mappers

import com.partymap.backend.api.dtos.TagDto
import com.partymap.backend.domain.tag.db.TagEntity

fun TagEntity.toDto(): TagDto =
    TagDto(
        slug = this.slug,
        label = this.label,
        color = this.color,
    )