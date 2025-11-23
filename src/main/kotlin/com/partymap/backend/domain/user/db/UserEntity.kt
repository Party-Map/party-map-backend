package com.partymap.backend.domain.user.db

import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.place.db.PlaceEntity
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class UserEntity(
    @Id
    @Column(updatable = false)
    var sub: UUID,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdDate: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedDate: Instant? = null,

    @ManyToMany
    @JoinTable(
        name = "user_liked_events",
        joinColumns = [JoinColumn(name = "user_sub")],       // references users.sub
        inverseJoinColumns = [JoinColumn(name = "event_id")] // references events.id (from BaseEntity)
    )
    var likedEvents: MutableSet<EventEntity> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "user_liked_places",
        joinColumns = [JoinColumn(name = "user_sub")],
        inverseJoinColumns = [JoinColumn(name = "place_id")]
    )
    var likedPlaces: MutableSet<PlaceEntity> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "user_liked_performers",
        joinColumns = [JoinColumn(name = "user_sub")],
        inverseJoinColumns = [JoinColumn(name = "performer_id")]
    )
    var likedPerformers: MutableSet<PerformerEntity> = mutableSetOf(),
)
