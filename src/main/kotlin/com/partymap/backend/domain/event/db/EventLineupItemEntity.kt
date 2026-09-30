package com.partymap.backend.domain.event.db

import com.partymap.backend.domain.performer.db.PerformerEntity
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.io.Serializable
import java.time.LocalDateTime

@Entity
class EventLineupItemEntity(
    @EmbeddedId
    var id: EventLineupItemId,

    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
)

@Embeddable
data class EventLineupItemId(
    @ManyToOne
    @JoinColumn(nullable = false)
    var event: EventEntity,

    @ManyToOne
    @JoinColumn(nullable = false)
    var performer: PerformerEntity,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
