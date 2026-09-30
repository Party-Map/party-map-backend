package com.partymap.backend.domain.common.db

import jakarta.persistence.Embeddable

@Embeddable
data class GeoPointEmbeddable(var latitude: Double = 0.0, var longitude: Double = 0.0)
