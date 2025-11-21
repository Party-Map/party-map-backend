package com.partymap.backend.domain.user.db

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findBySub(sub: UUID): UserEntity?
}