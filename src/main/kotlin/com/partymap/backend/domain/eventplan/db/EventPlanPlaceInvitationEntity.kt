package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.place.db.PlaceEntity
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.ManyToOne
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
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
