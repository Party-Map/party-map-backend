package com.partymap.backend.domain.place.db

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.Builder
import java.util.UUID

@Builder
@Entity
@Table(name = "places")
data class PlaceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
)