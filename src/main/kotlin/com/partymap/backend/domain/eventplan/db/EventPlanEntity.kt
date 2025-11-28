package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.user.db.UserEntity
import jakarta.persistence.*
import java.time.LocalTime

@Entity
class EventPlanEntity(
    @Column
    var title: String,

    @Column
    var description: String,

    @Column(name = "start_time", nullable = false)
    var start: LocalTime,

    @Column(name = "end_time", nullable = false)
    var end: LocalTime,

    @Column(nullable = true)
    var image: String? = null,

    @Column(nullable = true)
    var price: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var kind: EventType,

    @ElementCollection
    @CollectionTable(
        name = "event_links",
        joinColumns = [JoinColumn(name = "event_id")]
    )
    var links: MutableList<LinkEmbeddable> = mutableListOf(),

    @OneToOne(cascade = [(CascadeType.ALL)])
    var placeInvitation: EventPlanPlaceInvitationEntity,


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var owner: UserEntity,

    ) : BaseEntity()