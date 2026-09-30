package com.partymap.backend.domain.performer.dto

import com.partymap.backend.domain.common.dto.LinkDto
import java.util.UUID

data class PerformerDto(
    val id: UUID,
    val name: String,
    val genre: String,
    val bio: String,
    val image: String?,
    val links: List<LinkDto>,
)
