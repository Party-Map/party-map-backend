package com.partymap.backend.domain.performer.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.*

interface PerformerRepository : JpaRepository<PerformerEntity, UUID>, JpaSpecificationExecutor<PerformerEntity> {

    fun findAllByLikedByUsers_Sub(sub: UUID): List<PerformerEntity>
    fun findAllByOwner_Sub(sub: UUID): List<PerformerEntity>
}