package com.partymap.backend.domain.place.db

import com.partymap.backend.domain.event.db.EventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface PlaceRepository: JpaRepository<PlaceEntity, UUID>, JpaSpecificationExecutor<PlaceEntity> {
    fun findAllByLikedByUsers_Sub(sub: UUID): List<PlaceEntity>
}