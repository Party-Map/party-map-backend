package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.performer.db.PerformerEntity
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.io.Serializable
import java.time.LocalDateTime
import java.util.Objects

/** A performer asked to play in a slot of an event plan. */
@Entity
class EventPlanLineupInvitationEntity(
    @EmbeddedId
    var id: EventPlanLineupItemId,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: EventPlanLineupInvitationState = EventPlanLineupInvitationState.PENDING,
    @Column(nullable = false)
    var startTime: LocalDateTime,
    @Column(nullable = false)
    var endTime: LocalDateTime,
)

@Embeddable
class EventPlanLineupItemId(
    @ManyToOne
    @JoinColumn(name = "event_plan_id", nullable = false)
    var eventPlan: EventPlanEntity,
    @ManyToOne
    @JoinColumn(name = "performer_id", nullable = false)
    var performer: PerformerEntity,
) : Serializable {
    override fun equals(other: Any?): Boolean =
        other is EventPlanLineupItemId && eventPlan.id == other.eventPlan.id && performer.id == other.performer.id

    override fun hashCode(): Int = Objects.hash(eventPlan.id, performer.id)

    companion object {
        private const val serialVersionUID = 1L
    }
}
