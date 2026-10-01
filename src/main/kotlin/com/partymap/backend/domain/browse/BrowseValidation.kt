package com.partymap.backend.domain.browse

import com.partymap.backend.domain.browse.db.EventBrowseFilter
import com.partymap.backend.domain.browse.db.GeoOrigin
import com.partymap.backend.domain.browse.db.PlaceBrowseFilter
import com.partymap.backend.domain.browse.dto.EventBrowseQuery
import com.partymap.backend.domain.browse.dto.PlaceBrowseQuery
import com.partymap.backend.domain.common.exception.InvalidRequestException
import com.partymap.backend.domain.common.trimToNull
import com.partymap.backend.domain.search.SearchUtils
import java.time.LocalDateTime

/** Cross-field rules of the browse queries; the per-field ranges are bean-validation annotations on the queries. */
fun EventBrowseQuery.toFilter(now: LocalDateTime): EventBrowseFilter {
    val origin = resolveOrigin(lat, lon, radiusKm, sort)
    val from = this.from ?: now
    if (to != null && to.isBefore(from)) throw InvalidRequestException("to must not be before from.")
    return EventBrowseFilter(
        origin = origin,
        radiusKm = radiusKm,
        from = from,
        to = to,
        kind = kind,
        keywords = SearchUtils.prepareKeywords(q.orEmpty()),
        byDistance = origin != null && sort != "start",
        page = page,
        size = size,
    )
}

fun PlaceBrowseQuery.toFilter(): PlaceBrowseFilter {
    val origin = resolveOrigin(lat, lon, radiusKm, sort)
    return PlaceBrowseFilter(
        origin = origin,
        radiusKm = radiusKm,
        tag = tag.trimToNull(),
        keywords = SearchUtils.prepareKeywords(q.orEmpty()),
        byDistance = origin != null && sort != "name",
        page = page,
        size = size,
    )
}

/** Coordinates come as a pair; the radius and the distance sort need them. */
internal fun resolveOrigin(lat: Double?, lon: Double?, radiusKm: Double?, sort: String?): GeoOrigin? {
    if (lat != null && lon != null) return GeoOrigin(lat, lon)
    val problem = when {
        lat != null || lon != null -> "lat and lon must be given together."
        radiusKm != null -> "radiusKm needs lat and lon."
        sort == "distance" -> "Sorting by distance needs lat and lon."
        else -> return null
    }
    throw InvalidRequestException(problem)
}
