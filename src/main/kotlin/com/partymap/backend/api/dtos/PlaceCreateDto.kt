package com.partymap.backend.api.dtos

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