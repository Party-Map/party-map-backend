package com.partymap.backend.domain.performer.db

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.event.db.EventLineupItemEntity
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationEntity
import com.partymap.backend.domain.user.UserEntity
import jakarta.persistence.*

@Entity
class PerformerEntity(

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var genre: String,

    @Column(columnDefinition = "text")
    var bio: String,

    @Column(nullable = true)
    var image: String? = null,

    @ElementCollection
    @CollectionTable(
        name = "performer_links",
        joinColumns = [JoinColumn(name = "performer_id")]
    )
    var links: MutableList<LinkEmbeddable> = mutableListOf(),

    @ManyToMany(mappedBy = "likedPerformers")
    var likedByUsers: MutableSet<UserEntity> = mutableSetOf(),

    @OneToMany
    var lineupItems: MutableList<EventLineupItemEntity> = mutableListOf(),

    @OneToMany
    var lineupInvitations: MutableList<EventPlanLineupInvitationEntity> = mutableListOf(),

    // owner
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var owner: UserEntity,
) : BaseEntity()
