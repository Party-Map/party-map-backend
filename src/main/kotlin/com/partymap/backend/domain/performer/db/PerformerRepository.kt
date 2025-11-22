package com.partymap.backend.domain.performer.db

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PerformerRepository: JpaRepository<PerformerEntity, UUID> {
    fun findAllByOwnerSub(ownerId: UUID): List<PerformerEntity>
}