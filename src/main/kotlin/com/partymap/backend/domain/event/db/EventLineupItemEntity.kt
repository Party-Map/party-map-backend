package com.partymap.backend.domain.event.db

import com.partymap.backend.domain.performer.db.PerformerEntity
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.io.Serializable
import java.time.LocalDateTime
import java.util.Objects

/** One performer's slot in a published event. */
@Entity
class EventLineupItemEntity(
    @EmbeddedId
    var id: EventLineupItemId,
    @Column(nullable = false)
    var startTime: LocalDateTime,
    @Column(nullable = false)
    var endTime: LocalDateTime,
)

/** Equal when event and performer ids are, so ids compare correctly across persistence contexts. */
@Embeddable
class EventLineupItemId(
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    var event: EventEntity,
    @ManyToOne
    @JoinColumn(name = "performer_id", nullable = false)
    var performer: PerformerEntity,
) : Serializable {
    override fun equals(other: Any?): Boolean =
        other is EventLineupItemId && event.id == other.event.id && performer.id == other.performer.id

    override fun hashCode(): Int = Objects.hash(event.id, performer.id)

    companion object {
        private const val serialVersionUID = 1L
    }
}
