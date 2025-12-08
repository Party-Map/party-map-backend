package com.partymap.backend.domain.place.dto

import com.partymap.backend.domain.common.dto.GeoPointDto
import com.partymap.backend.domain.common.dto.LinkDto
import java.util.*

data class PlaceDto(
    val id: UUID,
    val name: String,
    val location: GeoPointDto,
    val address: String,
    val city: String,
    val description: String?,
    val image: String?,
    val tags: List<String>,
    val links: List<LinkDto>,
)
