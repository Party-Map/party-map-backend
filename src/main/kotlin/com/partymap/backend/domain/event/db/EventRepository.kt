package com.partymap.backend.domain.event.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.time.LocalDateTime
import java.util.*

interface EventRepository : JpaRepository<EventEntity, UUID>, JpaSpecificationExecutor<EventEntity> {
    fun findAllByPlace_Id(placeId: UUID): List<EventEntity>

    fun findAllByPlace_IdAndEndAfterOrderByStartAsc(
        placeId: UUID,
        endAfter: LocalDateTime,
    ): List<EventEntity>

    fun findAllByEndAfterOrderByPlace_IdAscStartAsc(
        endAfter: LocalDateTime,
    ): List<EventEntity>

    fun findAllByLikedByUsers_Sub(sub: UUID): List<EventEntity>

    fun findAllByOwner_Sub(sub: UUID): List<EventEntity>
}