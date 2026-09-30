package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.performer.db.PerformerEntity
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.ManyToOne
import java.io.Serializable
import java.time.LocalDateTime

@Entity
class EventPlanLineupInvitationEntity(
    @EmbeddedId
    var id: EventPlanLineupItemId,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: EventPlanLineupInvitationState = EventPlanLineupInvitationState.PENDING,

    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
)

@Embeddable
data class EventPlanLineupItemId(
    @ManyToOne
    var eventPlan: EventPlanEntity,

    @ManyToOne
    var performer: PerformerEntity,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
