package com.partymap.backend.domain.user

fun UserEntity.toDto() = UserDto(
    sub = this.sub,
)