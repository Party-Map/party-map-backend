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
import java.time.LocalDateTime

@Entity
class EventEntity(

    @Column(nullable = false)
    var title: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    var place: PlaceEntity,

    @Column(columnDefinition = "text")
    var description: String,

    @Column(name = "start_time", nullable = false)
    var start: LocalDateTime,

    @Column(name = "end_time", nullable = false)
    var end: LocalDateTime,

    @Column(nullable = true)
    var image: String? = null,

    @Column(nullable = true)
    var price: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var kind: EventType,

    @OneToMany(
        mappedBy = "id.event",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    var lineupItems: MutableList<EventLineupItemEntity> = mutableListOf(),

    @ElementCollection
    @CollectionTable(
        name = "event_links",
        joinColumns = [JoinColumn(name = "event_id")],
    )
    var links: MutableList<LinkEmbeddable> = mutableListOf(),

    @ManyToMany(mappedBy = "likedEvents")
    var likedByUsers: MutableList<UserEntity> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: UserEntity,
) : BaseEntity()
