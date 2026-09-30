package com.partymap.backend.domain.performer.db

import com.partymap.backend.domain.common.db.BaseEntity
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.user.UserEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne

/** Lineup items and invitations point at the performer; query them through their repositories. */
@Entity
class PerformerEntity(
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var genre: String,
    @Column(columnDefinition = "text", nullable = false)
    var bio: String,
    @Column(length = 2048)
    var image: String?,
    @ElementCollection
    @CollectionTable(name = "performer_links", joinColumns = [JoinColumn(name = "performer_id")])
    var links: MutableList<LinkEmbeddable>,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_sub", nullable = false)
    var owner: UserEntity,
) : BaseEntity() {
    @ManyToMany(mappedBy = "likedPerformers")
    var likedByUsers: MutableSet<UserEntity> = mutableSetOf()
}
