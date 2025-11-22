package com.partymap.backend.domain.performer.db

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.user.db.UserEntity
import com.partymap.backend.domain.tag.db.TagEntity
import jakarta.persistence.*

@Entity
@Table(name = "performers")
class PerformerEntity(

    @Column(nullable = false)
    var name: String,

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "genre_tag_id")
    var genreTag: TagEntity? = null,

    @ManyToMany
    @JoinTable(
        name = "performer_tags",
        joinColumns = [JoinColumn(name = "performer_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<TagEntity> = mutableSetOf(),

    // owner
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: UserEntity,
) : BaseEntity()
