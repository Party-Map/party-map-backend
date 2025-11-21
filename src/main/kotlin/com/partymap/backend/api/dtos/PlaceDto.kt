package com.partymap.backend.api.dtos

import java.util.UUID

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
