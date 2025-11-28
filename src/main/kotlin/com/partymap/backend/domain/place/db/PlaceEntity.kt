package com.partymap.backend.domain.place.db

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.common.db.GeoPointEmbeddable
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationEntity
import com.partymap.backend.domain.user.db.UserEntity
import jakarta.persistence.*

@Entity
class PlaceEntity(

    @Column(nullable = false)
    var name: String,

    @Embedded
    var location: GeoPointEmbeddable,

    @Column(nullable = false)
    var address: String,

    @Column(nullable = false)
    var city: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(nullable = true)
    var image: String? = null,

    @ElementCollection
    @CollectionTable(
        name = "place_tags",
        joinColumns = [JoinColumn(name = "place_id")]
    )
    @Column(nullable = false)
    var tags: MutableSet<String> = mutableSetOf(),

    @ElementCollection
    @CollectionTable(
        name = "place_links",
        joinColumns = [JoinColumn(name = "place_id")]
    )
    var links: MutableList<LinkEmbeddable> = mutableListOf(),

    @ManyToMany
    var likedByUsers: MutableSet<UserEntity> = mutableSetOf(),

    @OneToMany
    var eventPlanInvitations: MutableList<EventPlanPlaceInvitationEntity> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var owner: UserEntity,
) : BaseEntity()
