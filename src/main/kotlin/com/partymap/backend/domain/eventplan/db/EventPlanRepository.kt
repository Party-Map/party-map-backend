package com.partymap.backend.domain.eventplan.db

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EventPlanRepository : JpaRepository<EventPlanEntity, UUID> {
    fun findAllByOwnerSub(sub: UUID): List<EventPlanEntity>
}
