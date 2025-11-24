package com.partymap.backend.domain.performer.db

import com.partymap.backend.domain.event.db.EventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface PerformerRepository: JpaRepository<PerformerEntity, UUID>, JpaSpecificationExecutor<PerformerEntity> {

    fun findAllByLikedByUsers_Sub(sub: UUID): List<PerformerEntity>
}