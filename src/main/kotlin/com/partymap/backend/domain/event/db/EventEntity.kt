package com.partymap.backend.domain.event.db

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.tag.db.TagEntity
import com.partymap.backend.domain.user.db.UserEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "events")
class EventEntity(

    @Column(nullable = false)
    var title: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    var place: PlaceEntity,

    @Column(columnDefinition = "text")
    var description: String,

    @Column(name="start_time", nullable = false)
    var start: Instant,

    @Column(name="end_time", nullable = false)
    var end: Instant,

    @Column(nullable = true)
    var image: String? = null,

    @Column(nullable = true)
    var price: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kind_tag_id", nullable = false)
    var kindTag: TagEntity,

    @ManyToMany
    @JoinTable(
        name = "event_tags",
        joinColumns = [JoinColumn(name = "event_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<TagEntity> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "event_performers",
        joinColumns = [JoinColumn(name = "event_id")],
        inverseJoinColumns = [JoinColumn(name = "performer_id")]
    )
    var performers: MutableSet<PerformerEntity> = mutableSetOf(),

    @ElementCollection
    @CollectionTable(
        name = "event_links",
        joinColumns = [JoinColumn(name = "event_id")]
    )
    var links: MutableList<LinkEmbeddable> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: UserEntity,
) : BaseEntity()
