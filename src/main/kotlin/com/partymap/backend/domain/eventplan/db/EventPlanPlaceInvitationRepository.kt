package com.partymap.backend.domain.eventplan.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface EventPlanPlaceInvitationRepository :
    JpaRepository<EventPlanPlaceInvitationEntity, EventPlanPlaceInvitationEntityId> {

    @Query(
        "SELECT e FROM EventPlanPlaceInvitationEntity e " +
            "WHERE e.id.place.id = :placeId",
    )
    fun findAllByPlaceId(@Param("placeId") placeId: UUID): List<EventPlanPlaceInvitationEntity>

    @Query(
        "SELECT e FROM EventPlanPlaceInvitationEntity e " +
            "WHERE e.id.place.id = :placeId " +
            "AND e.id.eventPlan.id = :eventPlanId",
    )
    fun findByPlaceIdAndEventPlanId(
        @Param("placeId") placeId: UUID,
        @Param("eventPlanId") eventPlanId: UUID,
    ): Optional<EventPlanPlaceInvitationEntity>
}
