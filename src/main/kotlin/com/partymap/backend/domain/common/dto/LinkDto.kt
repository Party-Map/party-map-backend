package com.partymap.backend.domain.common.dto

import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.common.db.LinkType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LinkDto(
    val type: LinkType,
    @field:NotBlank
    @field:Size(max = 2048)
    val url: String,
)

fun LinkEmbeddable.toDto() = LinkDto(type, url)

fun List<LinkDto>?.toEmbeddables(): MutableList<LinkEmbeddable> =
    orEmpty().map { LinkEmbeddable(it.type, it.url.trim()) }.toMutableList()
