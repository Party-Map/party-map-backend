package com.partymap.backend.domain.eventplan.db

import org.springframework.data.jpa.repository.JpaRepository

interface EventPlanPlaceInvitationEntityRepository :
    JpaRepository<EventPlanPlaceInvitationEntity, EventPlanPlaceInvitationEntityId>
