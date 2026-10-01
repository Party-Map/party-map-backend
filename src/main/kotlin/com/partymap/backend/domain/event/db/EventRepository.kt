package com.partymap.backend.domain.event.db

import com.partymap.backend.domain.browse.db.BrowseEventRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

interface EventRepository :
    JpaRepository<EventEntity, UUID>,
    JpaSpecificationExecutor<EventEntity>,
    BrowseEventRepository {
    fun findAllByPlaceIdOrderByStartAsc(placeId: UUID): List<EventEntity>

    @Query(
        "SELECT DISTINCT e FROM EventEntity e JOIN e.lineupItems item " +
            "WHERE item.id.performer.id = :performerId ORDER BY e.start",
    )
    fun findAllByPerformerId(@Param("performerId") performerId: UUID): List<EventEntity>

    fun findAllByEndAfterOrderByPlaceIdAscStartAsc(endAfter: LocalDateTime): List<EventEntity>

    fun findAllByLikedByUsersSub(sub: UUID): List<EventEntity>

    fun findAllByOwnerSubOrderByStartAsc(sub: UUID): List<EventEntity>
}
