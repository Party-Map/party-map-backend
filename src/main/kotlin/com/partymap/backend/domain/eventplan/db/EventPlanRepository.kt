package com.partymap.backend.domain.eventplan.db

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EventPlanRepository : JpaRepository<EventPlanEntity, UUID> {
    fun findAllByOwner_Sub(sub: UUID): List<EventPlanEntity>

}
