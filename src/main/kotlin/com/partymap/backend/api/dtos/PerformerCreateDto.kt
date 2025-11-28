package com.partymap.backend.api.dtos

data class PerformerCreateDto(
    val name: String,
    val genre: String,
    val bio: String,
    val image: String? = null,
    val links: List<LinkDto>? = null,
)
