package com.partymap.backend.domain.place.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface PlaceRepository :
    JpaRepository<PlaceEntity, UUID>,
    JpaSpecificationExecutor<PlaceEntity> {
    fun findAllByLikedByUsersSub(sub: UUID): List<PlaceEntity>

    fun findAllByOwnerSub(sub: UUID): List<PlaceEntity>

    /** Places inside the box (no antimeridian wrap: the map never spans it). */
    fun findAllByLocationLatitudeBetweenAndLocationLongitudeBetween(
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double,
    ): List<PlaceEntity>
}
