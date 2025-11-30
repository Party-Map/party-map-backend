package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.performer.db.PerformerEntity
import jakarta.persistence.*
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
    var performer: PerformerEntity
) : Serializable