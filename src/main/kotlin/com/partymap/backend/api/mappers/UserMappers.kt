package com.partymap.backend.api.mappers

import com.partymap.backend.api.dtos.UserDto
import com.partymap.backend.domain.user.db.UserEntity

fun UserEntity.toDto() = UserDto(
    sub = this.sub,
)