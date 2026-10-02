package com.partymap.backend.domain.common.geo

import org.springframework.core.io.ClassPathResource
import tools.jackson.databind.json.JsonMapper

/**
 * The country's outline (OpenStreetMap relation 21335, simplified to about 200 m; the same file the frontend draws
 * its basemap with), for the rule that every place lies inside Hungary.
 */
object Hungary {
    private const val OUTLINE = "geo/hungary.json"

    /** The outline as (longitude, latitude) pairs, closed. */
    val outline: List<Pair<Double, Double>> by lazy { load() }

    private val minLongitude by lazy { outline.minOf { it.first } }
    private val maxLongitude by lazy { outline.maxOf { it.first } }
    private val minLatitude by lazy { outline.minOf { it.second } }
    private val maxLatitude by lazy { outline.maxOf { it.second } }

    /** Whether the point lies inside the border (ray casting after a bounding-box check). */
    fun contains(latitude: Double, longitude: Double): Boolean {
        if (latitude !in minLatitude..maxLatitude || longitude !in minLongitude..maxLongitude) return false
        var inside = false
        var j = outline.size - 1
        for (i in outline.indices) {
            val (xi, yi) = outline[i]
            val (xj, yj) = outline[j]
            val crosses = (yi > latitude) != (yj > latitude) &&
                longitude < (xj - xi) * (latitude - yi) / (yj - yi) + xi
            if (crosses) inside = !inside
            j = i
        }
        return inside
    }

    private fun load(): List<Pair<Double, Double>> {
        val root = JsonMapper.shared().readTree(ClassPathResource(OUTLINE).inputStream)
        val ring = root.path("geometry").path("coordinates").path(0)
        require(ring.isArray && ring.size() > 2) { "$OUTLINE holds no polygon ring" }
        return (0 until ring.size()).map { i -> ring.get(i).path(0).asDouble() to ring.get(i).path(1).asDouble() }
    }
}
