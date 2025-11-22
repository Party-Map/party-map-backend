package com.partymap.backend.api.dtos

import java.util.UUID

data class PerformerDto(
    val id: UUID,
    val name: String,
    val genreTag: TagDto?,
    val tags: List<TagDto>?,
    val bio: String?,
    val image: String?,
    val links: List<LinkDto>,
)
