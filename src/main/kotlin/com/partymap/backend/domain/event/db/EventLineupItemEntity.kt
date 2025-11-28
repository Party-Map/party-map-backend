package com.partymap.backend.domain.event.db

import com.partymap.backend.domain.performer.db.PerformerEntity
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalTime

@Entity
class EventLineupItemEntity(
    @EmbeddedId
    var id: EventLineupItemId,

    var startTime: LocalTime,
    var endTime: LocalTime,
)

@Embeddable
data class EventLineupItemId(
    @ManyToOne
    @JoinColumn(nullable = false)
    var event: EventEntity,

    @ManyToOne
    @JoinColumn(nullable = false)
    var performer: PerformerEntity
) : Serializable