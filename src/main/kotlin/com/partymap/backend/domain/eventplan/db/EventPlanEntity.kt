package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class EventPlanEntity(
    @Column
    var title: String,

    @Column
    var description: String,

    @Column(nullable = false)
    var startDateTime: LocalDateTime,

    @Column(nullable = false)
    var endDateTime: LocalDateTime,

    @Column(nullable = true)
    var image: String?,

    @Column(nullable = true)
    var price: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var kind: EventType,

    @ElementCollection
    @CollectionTable(
        name = "event_links",
        joinColumns = [JoinColumn(name = "event_id")]
    )
    var links: MutableList<LinkEmbeddable> = mutableListOf(),

    // Optional one-to-one relationship is not possible in JPA
    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    var placeInvitations: MutableList<EventPlanPlaceInvitationEntity> = mutableListOf(),

    @OneToMany
    var lineupInvitations: MutableList<EventPlanLineupInvitationEntity> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var owner: UserEntity,

    ) : BaseEntity()