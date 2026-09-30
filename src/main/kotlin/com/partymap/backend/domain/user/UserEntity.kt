package com.partymap.backend.domain.user

import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.place.db.PlaceEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Entity
@EntityListeners(AuditingEntityListener::class)
class UserEntity(
    @Id
    @Column(updatable = false)
    var sub: UUID,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDate: Instant? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedDate: Instant? = null,

    @ManyToMany
    @JoinTable(
        name = "user_liked_events",
        joinColumns = [JoinColumn(name = "user_sub")],
        inverseJoinColumns = [JoinColumn(name = "event_id")],
    )
    var likedEvents: MutableSet<EventEntity> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "user_liked_places",
        joinColumns = [JoinColumn(name = "user_sub")],
        inverseJoinColumns = [JoinColumn(name = "place_id")],
    )
    var likedPlaces: MutableSet<PlaceEntity> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "user_liked_performers",
        joinColumns = [JoinColumn(name = "user_sub")],
        inverseJoinColumns = [JoinColumn(name = "performer_id")],
    )
    var likedPerformers: MutableSet<PerformerEntity> = mutableSetOf(),
)
