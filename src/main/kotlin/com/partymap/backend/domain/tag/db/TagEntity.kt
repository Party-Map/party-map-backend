package com.partymap.backend.domain.tag.db

import com.partymap.backend.domain.common.db.BaseEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint



@Entity
@Table(
    name = "tags",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_tags_slug", columnNames = ["slug"])
    ]
)
class TagEntity(
    @Id
    var slug: String, // "disco"

    @Column(nullable = false)
    var label : String, // "Disco"

    @Column(nullable = true)
    var color: String? = null,

    // Will be useful on admin page to limit tag usage, so terrace can't be a performer's genre
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "tag_usages",
        joinColumns = [JoinColumn(name = "tag_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "usage", nullable = false)
    var usages: MutableSet<TagUsage> = mutableSetOf(),
)


