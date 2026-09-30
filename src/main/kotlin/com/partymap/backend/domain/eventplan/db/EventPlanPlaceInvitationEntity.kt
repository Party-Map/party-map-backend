package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.place.db.PlaceEntity
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.io.Serializable
import java.util.Objects

/** The venue asked to host an event plan. */
@Entity
class EventPlanPlaceInvitationEntity(
    @EmbeddedId
    var id: EventPlanPlaceInvitationEntityId,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: EventPlanPlaceInvitationState = EventPlanPlaceInvitationState.PENDING,
)

@Embeddable
class EventPlanPlaceInvitationEntityId(
    @ManyToOne
    @JoinColumn(name = "event_plan_id", nullable = false)
    var eventPlan: EventPlanEntity,
    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    var place: PlaceEntity,
) : Serializable {
    override fun equals(other: Any?): Boolean =
        other is EventPlanPlaceInvitationEntityId && eventPlan.id == other.eventPlan.id && place.id == other.place.id

    override fun hashCode(): Int = Objects.hash(eventPlan.id, place.id)

    companion object {
        private const val serialVersionUID = 1L
    }
}
