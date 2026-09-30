package com.partymap.backend.domain.place.dto

import java.util.UUID

data class PlaceAdminListItemDto(val id: UUID, val name: String, val address: String, val city: String)
