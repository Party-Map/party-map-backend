package com.partymap.backend.domain.eventplan.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface EventPlanPlaceInvitationRepository :
    JpaRepository<EventPlanPlaceInvitationEntity, EventPlanPlaceInvitationEntityId> {
    @Query(
        "SELECT i FROM EventPlanPlaceInvitationEntity i JOIN FETCH i.id.eventPlan plan " +
            "WHERE i.id.place.id = :placeId ORDER BY plan.startDateTime",
    )
    fun findAllByPlaceId(@Param("placeId") placeId: UUID): List<EventPlanPlaceInvitationEntity>

    @Query(
        "SELECT i FROM EventPlanPlaceInvitationEntity i " +
            "WHERE i.id.place.id = :placeId AND i.id.eventPlan.id = :eventPlanId",
    )
    fun findByPlaceIdAndEventPlanId(
        @Param("placeId") placeId: UUID,
        @Param("eventPlanId") eventPlanId: UUID,
    ): Optional<EventPlanPlaceInvitationEntity>
}
