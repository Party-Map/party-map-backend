package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.place.db.PlaceEntity
import jakarta.persistence.*
import java.io.Serializable

@Entity
class EventPlanPlaceInvitationEntity(
    @EmbeddedId
    var id: EventPlanPlaceInvitationEntityId,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: EventPlanPlaceInvitationState = EventPlanPlaceInvitationState.PENDING,
)

@Embeddable
data class EventPlanPlaceInvitationEntityId(
    @ManyToOne
    var eventPlan: EventPlanEntity,

    @ManyToOne
    var place: PlaceEntity,
) : Serializable
