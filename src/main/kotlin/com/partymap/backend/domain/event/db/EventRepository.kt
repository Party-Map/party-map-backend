package com.partymap.backend.domain.event.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface EventRepository: JpaRepository<EventEntity, UUID>, JpaSpecificationExecutor<EventEntity> {
    fun findAllByPlace_Id(placeId: UUID): List<EventEntity>
    fun findAllByPerformers_Id(performerId: UUID): List<EventEntity>
}