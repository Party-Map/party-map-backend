package com.partymap.backend.domain.place.db

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.common.db.GeoPointEmbeddable
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.user.UserEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne

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
        joinColumns = [JoinColumn(name = "place_id")],
    )
    @Column(nullable = false)
    var tags: MutableSet<String> = mutableSetOf(),

    @ElementCollection
    @CollectionTable(
        name = "place_links",
        joinColumns = [JoinColumn(name = "place_id")],
    )
    var links: MutableList<LinkEmbeddable> = mutableListOf(),

    @ManyToMany(mappedBy = "likedPlaces")
    var likedByUsers: MutableSet<UserEntity> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var owner: UserEntity,
) : BaseEntity()
