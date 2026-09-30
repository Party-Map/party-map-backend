package com.partymap.backend.domain.place.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface PlaceRepository :
    JpaRepository<PlaceEntity, UUID>,
    JpaSpecificationExecutor<PlaceEntity> {
    fun findAllByLikedByUsersSub(sub: UUID): List<PlaceEntity>
    fun findAllByOwnerSub(ownerId: UUID): List<PlaceEntity>
}
