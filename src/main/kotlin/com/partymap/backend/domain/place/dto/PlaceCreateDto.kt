package com.partymap.backend.domain.place.dto

import com.partymap.backend.domain.common.dto.GeoPointDto
import com.partymap.backend.domain.common.dto.LinkDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/** Body of the create and update requests. Blank tags are dropped. */
data class PlaceCreateDto(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,
    @field:Valid
    val location: GeoPointDto,
    @field:Size(max = 255)
    val address: String,
    @field:NotBlank
    @field:Size(max = 255)
    val city: String,
    val description: String? = null,
    @field:Size(max = 2048)
    val image: String? = null,
    val tags: List<String>? = null,
    @field:Valid
    val links: List<LinkDto>? = null,
)
