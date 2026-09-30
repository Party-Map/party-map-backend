package com.partymap.backend.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    /** Inserts the user unless the row exists; safe when several requests of a new user arrive at once. */
    @Modifying
    @Query(
        value = "INSERT INTO user_entity (sub, created_at, updated_at) VALUES (:sub, now(), now()) " +
            "ON CONFLICT (sub) DO NOTHING",
        nativeQuery = true,
    )
    fun insertIfAbsent(@Param("sub") sub: UUID): Int
}
