package com.partymap.backend.api.dtos

data class TagDto(
    val slug: String,
    val label: String,
    val color: String?,
)
