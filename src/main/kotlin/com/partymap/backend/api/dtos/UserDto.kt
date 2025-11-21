package com.partymap.backend.api.dtos

import java.util.UUID

data class UserDto(
    val id: UUID?,
    val sub: UUID,
)
