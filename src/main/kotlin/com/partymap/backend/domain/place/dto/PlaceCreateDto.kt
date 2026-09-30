package com.partymap.backend.domain.place.dto

import com.partymap.backend.domain.common.dto.GeoPointDto
import com.partymap.backend.domain.common.dto.LinkDto

data class PlaceCreateDto(
    val name: String,
    val location: GeoPointDto,
    val address: String,
    val city: String,
    val description: String? = null,
    val image: String? = null,
    val tags: List<String>? = null,
    val links: List<LinkDto>? = null,
)
