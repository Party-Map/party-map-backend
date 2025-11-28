package com.partymap.backend.domain.performer.dto

import com.partymap.backend.domain.like.dto.LinkDto
import java.util.*

data class PerformerDto(
    val id: UUID,
    val name: String,
    val genre: String,
    val bio: String,
    val image: String?,
    val links: List<LinkDto>,
)
