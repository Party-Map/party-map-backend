package com.partymap.backend.domain.common.db

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class LinkEmbeddable(

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: LinkType = LinkType.WEBSITE,

    @Column(name = "url", nullable = false)
    var url: String = "",
)
