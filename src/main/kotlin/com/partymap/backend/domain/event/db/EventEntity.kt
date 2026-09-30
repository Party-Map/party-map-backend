package com.partymap.backend.domain.event.db

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.user.UserEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import java.time.LocalDateTime

/** A published event; created only by publishing an event plan. */
@Entity
class EventEntity(
    @Column(nullable = false)
    var title: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    var place: PlaceEntity,
    @Column(columnDefinition = "text", nullable = false)
    var description: String,
    @Column(name = "start_time", nullable = false)
    var start: LocalDateTime,
    @Column(name = "end_time", nullable = false)
    var end: LocalDateTime,
    @Column(length = 2048)
    var image: String?,
    var price: String?,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var kind: EventType,
    @ElementCollection
    @CollectionTable(name = "event_links", joinColumns = [JoinColumn(name = "event_id")])
    var links: MutableList<LinkEmbeddable>,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: UserEntity,
) : BaseEntity() {
    @OneToMany(mappedBy = "id.event", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("startTime")
    var lineupItems: MutableList<EventLineupItemEntity> = mutableListOf()

    @ManyToMany(mappedBy = "likedEvents")
    var likedByUsers: MutableSet<UserEntity> = mutableSetOf()
}
