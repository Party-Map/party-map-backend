package com.partymap.backend.domain.place.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.*

interface PlaceRepository :
    JpaRepository<PlaceEntity, UUID>,
    JpaSpecificationExecutor<PlaceEntity> {
    fun findAllByLikedByUsers_Sub(sub: UUID): List<PlaceEntity>
    fun findAllByOwner_Sub(ownerId: UUID): List<PlaceEntity>
}
