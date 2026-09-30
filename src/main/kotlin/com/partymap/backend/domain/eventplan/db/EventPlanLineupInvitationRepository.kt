package com.partymap.backend.domain.eventplan.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface EventPlanLineupInvitationRepository :
    JpaRepository<EventPlanLineupInvitationEntity, EventPlanLineupItemId> {
    @Query(
        "SELECT i FROM EventPlanLineupInvitationEntity i JOIN FETCH i.id.eventPlan plan " +
            "WHERE i.id.performer.id = :performerId ORDER BY i.startTime",
    )
    fun findAllByPerformerId(@Param("performerId") performerId: UUID): List<EventPlanLineupInvitationEntity>

    @Query(
        "SELECT i FROM EventPlanLineupInvitationEntity i " +
            "WHERE i.id.performer.id = :performerId AND i.id.eventPlan.id = :eventPlanId",
    )
    fun findByPerformerIdAndEventPlanId(
        @Param("performerId") performerId: UUID,
        @Param("eventPlanId") eventPlanId: UUID,
    ): Optional<EventPlanLineupInvitationEntity>
}
