package com.partymap.backend.domain.place.db

import com.partymap.backend.domain.browse.db.BrowsePlaceRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface PlaceRepository :
    JpaRepository<PlaceEntity, UUID>,
    JpaSpecificationExecutor<PlaceEntity>,
    BrowsePlaceRepository {
    fun findAllByLikedByUsersSub(sub: UUID): List<PlaceEntity>

    fun findAllByOwnerSub(sub: UUID): List<PlaceEntity>

    /** Places inside the box (no antimeridian wrap: the map never spans it). */
    fun findAllByLocationLatitudeBetweenAndLocationLongitudeBetween(
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double,
    ): List<PlaceEntity>

    /** Every tag with the number of places carrying it, most used first, then alphabetically. */
    @Query("SELECT t AS tag, COUNT(p) AS count FROM PlaceEntity p JOIN p.tags t GROUP BY t ORDER BY COUNT(p) DESC, t")
    fun countTags(): List<TagCount>
}

/** Projection of [PlaceRepository.countTags]. */
interface TagCount {
    val tag: String
    val count: Long
}
