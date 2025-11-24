package com.partymap.backend.domain.event.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.time.Instant
import java.util.UUID

interface EventRepository: JpaRepository<EventEntity, UUID>, JpaSpecificationExecutor<EventEntity> {
    fun findAllByPlace_Id(placeId: UUID): List<EventEntity>

    fun findAllByPerformers_Id(performerId: UUID): List<EventEntity>

    fun findAllByPlace_IdAndEndAfterOrderByStartAsc(
        placeId: UUID,
        endAfter: Instant,
    ): List<EventEntity>

    fun findAllByEndAfterOrderByPlace_IdAscStartAsc(
        endAfter: Instant,
    ): List<EventEntity>

    fun findAllByLikedByUsers_Sub(sub: UUID): List<EventEntity>
}