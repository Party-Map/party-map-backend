package com.partymap.backend.domain.performer.dto

import com.partymap.backend.domain.common.dto.LinkDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/** Body of the create and update requests. */
data class PerformerCreateDto(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,
    @field:NotBlank
    @field:Size(max = 255)
    val genre: String,
    val bio: String = "",
    @field:Size(max = 2048)
    val image: String? = null,
    @field:Valid
    val links: List<LinkDto>? = null,
)
