package com.partymap.backend.domain.place.db

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PlaceRepository: JpaRepository<PlaceEntity, UUID> {
    fun findAllByOwnerId(ownerId: UUID): List<PlaceEntity>
}