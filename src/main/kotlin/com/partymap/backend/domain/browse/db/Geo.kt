package com.partymap.backend.domain.browse.db

import com.partymap.backend.domain.common.db.GeoPointEmbeddable
import com.partymap.backend.domain.place.BoundingBox
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** The caller's position that distances are measured from. */
data class GeoOrigin(val latitude: Double, val longitude: Double) {
    /**
     * A box that surely contains the circle of [radiusKm] around the origin, so the (latitude, longitude) index can
     * pre-filter before the exact distance is compared. Near the poles the box spans every longitude.
     */
    fun boundingBox(radiusKm: Double): BoundingBox {
        val latitudeDelta = radiusKm / KM_PER_DEGREE
        val minLatitude = (latitude - latitudeDelta).coerceAtLeast(-MAX_LATITUDE)
        val maxLatitude = (latitude + latitudeDelta).coerceAtMost(MAX_LATITUDE)
        if (abs(latitude) + latitudeDelta >= POLAR_LATITUDE) {
            return BoundingBox(-MAX_LONGITUDE, minLatitude, MAX_LONGITUDE, maxLatitude)
        }
        val longitudeDelta = radiusKm / (KM_PER_DEGREE * cos(Math.toRadians(latitude)))
        return BoundingBox(
            minLongitude = (longitude - longitudeDelta).coerceAtLeast(-MAX_LONGITUDE),
            minLatitude = minLatitude,
            maxLongitude = (longitude + longitudeDelta).coerceAtMost(MAX_LONGITUDE),
            maxLatitude = maxLatitude,
        )
    }

    private companion object {
        const val KM_PER_DEGREE = 111.32
        const val MAX_LATITUDE = 90.0
        const val MAX_LONGITUDE = 180.0
        const val POLAR_LATITUDE = 89.0
    }
}

/** Great-circle distance as a criteria expression (spherical law of cosines, in kilometres). */
object Haversine {
    const val EARTH_RADIUS_KM = 6371.0088

    /**
     * Distance between [origin] and the embedded [location]. The cosine is clamped to [-1, 1] because floating-point
     * rounding can push it a hair above 1 for a place at the origin, which `acos` rejects.
     */
    fun km(cb: CriteriaBuilder, location: Path<GeoPointEmbeddable>, origin: GeoOrigin): Expression<Double> {
        val type = Double::class.javaObjectType
        val originLatitude = Math.toRadians(origin.latitude)
        val originLongitude = Math.toRadians(origin.longitude)
        val latitude = cb.function("radians", type, location.get<Double>("latitude"))
        val longitude = cb.function("radians", type, location.get<Double>("longitude"))
        val longitudeDifference = cb.function("cos", type, cb.diff(longitude, originLongitude))
        val cosine = cb.sum(
            cb.prod(cb.prod(cb.function("cos", type, latitude), cos(originLatitude)), longitudeDifference),
            cb.prod(cb.function("sin", type, latitude), sin(originLatitude)),
        )
        val clamped = cb.function(
            "least",
            type,
            cb.literal(1.0),
            cb.function("greatest", type, cb.literal(-1.0), cosine),
        )
        return cb.prod(cb.function("acos", type, clamped), EARTH_RADIUS_KM)
    }

    /** Inside the index-friendly box around [origin] and within [radiusKm] of it. */
    fun withinRadius(
        cb: CriteriaBuilder,
        location: Path<GeoPointEmbeddable>,
        distance: Expression<Double>,
        origin: GeoOrigin,
        radiusKm: Double,
    ): List<Predicate> {
        val box = origin.boundingBox(radiusKm)
        return listOf(
            cb.between(location.get("latitude"), box.minLatitude, box.maxLatitude),
            cb.between(location.get("longitude"), box.minLongitude, box.maxLongitude),
            cb.le(distance, radiusKm),
        )
    }
}
