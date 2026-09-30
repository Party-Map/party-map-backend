package com.partymap.backend.domain.like

import com.partymap.backend.domain.common.exception.NotFoundException
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.place.db.PlaceRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Likes live in the `user_liked_*` join tables. They are written with single SQL statements (insert-if-absent,
 * delete) so that double clicks and parallel tabs cannot collide on the primary key.
 */
@Service
@Transactional
class LikeService(
    private val jdbc: JdbcClient,
    private val eventRepository: EventRepository,
    private val placeRepository: PlaceRepository,
    private val performerRepository: PerformerRepository,
) {
    @Transactional(readOnly = true)
    fun isLiked(sub: UUID, target: LikeTarget, id: UUID): Boolean {
        requireExists(target, id)
        return jdbc.sql(
            "SELECT EXISTS (SELECT 1 FROM ${target.table} WHERE user_sub = :sub AND ${target.column} = :id)",
        )
            .param("sub", sub)
            .param("id", id)
            .query(Boolean::class.java)
            .single()
    }

    /** Stores [liked] for the user (who must already exist) and returns it. */
    fun setLiked(sub: UUID, target: LikeTarget, id: UUID, liked: Boolean): Boolean {
        requireExists(target, id)
        val sql = if (liked) {
            "INSERT INTO ${target.table} (user_sub, ${target.column}) VALUES (:sub, :id) ON CONFLICT DO NOTHING"
        } else {
            "DELETE FROM ${target.table} WHERE user_sub = :sub AND ${target.column} = :id"
        }
        jdbc.sql(sql).param("sub", sub).param("id", id).update()
        return liked
    }

    private fun requireExists(target: LikeTarget, id: UUID) {
        val exists = when (target) {
            LikeTarget.EVENT -> eventRepository.existsById(id)
            LikeTarget.PLACE -> placeRepository.existsById(id)
            LikeTarget.PERFORMER -> performerRepository.existsById(id)
        }
        if (!exists) throw NotFoundException(target.label, id)
    }
}
