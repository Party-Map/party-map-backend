package com.partymap.backend.domain.performer.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface PerformerRepository :
    JpaRepository<PerformerEntity, UUID>,
    JpaSpecificationExecutor<PerformerEntity> {
    fun findAllByLikedByUsersSub(sub: UUID): List<PerformerEntity>

    fun findAllByOwnerSub(sub: UUID): List<PerformerEntity>

    /** Every genre with the number of performers in it, most common first, then alphabetically. */
    @Query(
        "SELECT p.genre AS genre, COUNT(p) AS count FROM PerformerEntity p " +
            "GROUP BY p.genre ORDER BY COUNT(p) DESC, p.genre",
    )
    fun countGenres(): List<GenreCount>
}

/** Projection of [PerformerRepository.countGenres]. */
interface GenreCount {
    val genre: String
    val count: Long
}
