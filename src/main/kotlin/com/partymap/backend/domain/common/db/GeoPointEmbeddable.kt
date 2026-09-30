package com.partymap.backend.domain.common.db

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class GeoPointEmbeddable(
    @Column(nullable = false)
    var latitude: Double = 0.0,
    @Column(nullable = false)
    var longitude: Double = 0.0,
)
