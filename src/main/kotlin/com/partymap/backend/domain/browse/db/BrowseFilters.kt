package com.partymap.backend.domain.browse.db

import com.partymap.backend.domain.event.db.EventType
import java.time.LocalDateTime

/** Resolved event filters: validated, with the defaults applied and the keywords prepared. */
data class EventBrowseFilter(
    val origin: GeoOrigin?,
    val radiusKm: Double?,
    val from: LocalDateTime,
    val to: LocalDateTime?,
    val kind: EventType?,
    val keywords: List<String>,
    val byDistance: Boolean,
    val page: Int,
    val size: Int,
)

/** Resolved place filters. */
data class PlaceBrowseFilter(
    val origin: GeoOrigin?,
    val radiusKm: Double?,
    val tag: String?,
    val keywords: List<String>,
    val byDistance: Boolean,
    val page: Int,
    val size: Int,
)

/** One page of entities with their distance from the caller (null without coordinates) and the total match count. */
data class Ranked<T>(val items: List<Pair<T, Double?>>, val total: Long)
