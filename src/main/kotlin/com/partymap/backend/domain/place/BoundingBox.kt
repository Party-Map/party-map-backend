package com.partymap.backend.domain.place

import com.partymap.backend.domain.common.exception.InvalidRequestException

/** A map viewport, parsed from `bbox=minLon,minLat,maxLon,maxLat` (the order OpenStreetMap and GeoJSON use). */
data class BoundingBox(
    val minLongitude: Double,
    val minLatitude: Double,
    val maxLongitude: Double,
    val maxLatitude: Double,
) {
    private val isValid: Boolean
        get() = minLongitude <= maxLongitude &&
            minLatitude <= maxLatitude &&
            listOf(minLatitude, maxLatitude).all { it in -MAX_LATITUDE..MAX_LATITUDE } &&
            listOf(minLongitude, maxLongitude).all { it in -MAX_LONGITUDE..MAX_LONGITUDE }

    companion object {
        private const val MAX_LATITUDE = 90.0
        private const val MAX_LONGITUDE = 180.0
        private const val PARTS = 4

        fun parse(value: String): BoundingBox {
            val numbers = value.split(',').map { it.trim().toDoubleOrNull() }
            val box = numbers.takeIf { it.size == PARTS && it.all { number -> number != null } }
                ?.let { BoundingBox(it[0]!!, it[1]!!, it[2]!!, it[3]!!) }
            if (box == null || !box.isValid) {
                throw InvalidRequestException(
                    "bbox must be minLon,minLat,maxLon,maxLat with latitudes in [-90, 90], longitudes in " +
                        "[-180, 180] and each minimum not above its maximum.",
                )
            }
            return box
        }
    }
}
