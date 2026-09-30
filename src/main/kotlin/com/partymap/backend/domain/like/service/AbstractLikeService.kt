package com.partymap.backend.domain.like.service

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

abstract class AbstractLikeService<E : BaseEntity>(
    private val entityRepository: JpaRepository<E, UUID>,
    private val getLikedCollection: (UserEntity) -> MutableSet<E>,
) {

    @Transactional(readOnly = true)
    open fun isLiked(user: UserEntity, entityId: UUID): Boolean = getLikedCollection(user).any { it.id == entityId }

    @Transactional
    open fun like(user: UserEntity, entityId: UUID) {
        val entity = entityRepository.findById(entityId)
            .orElseThrow { NoSuchElementException("Entity $entityId not found") }

        getLikedCollection(user).add(entity)
    }

    @Transactional
    open fun unlike(user: UserEntity, entityId: UUID) {
        getLikedCollection(user).removeIf { it.id == entityId }
    }
}
