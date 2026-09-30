package com.partymap.backend.domain.event.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.time.LocalDateTime
import java.util.UUID

interface EventRepository :
    JpaRepository<EventEntity, UUID>,
    JpaSpecificationExecutor<EventEntity> {
    fun findAllByPlaceId(placeId: UUID): List<EventEntity>

    fun findAllByEndAfterOrderByPlaceIdAscStartAsc(endAfter: LocalDateTime): List<EventEntity>

    fun findAllByLikedByUsersSub(sub: UUID): List<EventEntity>

    fun findAllByOwnerSub(sub: UUID): List<EventEntity>
}
