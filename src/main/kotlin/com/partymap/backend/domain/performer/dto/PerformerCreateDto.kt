package com.partymap.backend.domain.performer.dto

import com.partymap.backend.domain.common.dto.LinkDto

data class PerformerCreateDto(
    val name: String,
    val genre: String,
    val bio: String,
    val image: String? = null,
    val links: List<LinkDto>? = null,
)
