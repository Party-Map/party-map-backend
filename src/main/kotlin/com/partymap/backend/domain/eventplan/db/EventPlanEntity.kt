package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.event.db.EventType
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
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import java.time.LocalDateTime

/**
 * An organizer's draft. It holds at most one place invitation and any number of lineup invitations; publishing turns
 * it into an event and deletes it together with its invitations.
 */
@Entity
class EventPlanEntity(
    @Column(nullable = false)
    var title: String,
    @Column(columnDefinition = "text", nullable = false)
    var description: String,
    @Column(nullable = false)
    var startDateTime: LocalDateTime,
    @Column(nullable = false)
    var endDateTime: LocalDateTime,
    @Column(length = 2048)
    var image: String?,
    var price: String?,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var kind: EventType,
    @ElementCollection
    @CollectionTable(name = "event_plan_links", joinColumns = [JoinColumn(name = "event_plan_id")])
    var links: MutableList<LinkEmbeddable>,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_sub", nullable = false)
    var owner: UserEntity,
) : BaseEntity() {
    @OneToMany(mappedBy = "id.eventPlan", cascade = [CascadeType.ALL], orphanRemoval = true)
    var placeInvitations: MutableList<EventPlanPlaceInvitationEntity> = mutableListOf()

    @OneToMany(mappedBy = "id.eventPlan", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("startTime")
    var lineupInvitations: MutableList<EventPlanLineupInvitationEntity> = mutableListOf()

    /** The single place invitation, if one was sent. */
    val placeInvitation: EventPlanPlaceInvitationEntity? get() = placeInvitations.firstOrNull()
}
