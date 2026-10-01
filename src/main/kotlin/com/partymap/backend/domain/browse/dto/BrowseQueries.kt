package com.partymap.backend.domain.browse.dto

import com.partymap.backend.domain.event.db.EventType
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

/** Limits shared by the browse queries. */
object BrowseLimits {
    const val MAX_PAGE_SIZE = 50L
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_QUERY_LENGTH = 100
    const val MAX_TAG_LENGTH = 40
    const val MAX_RADIUS_KM = "1000.0"
}

/**
 * Filters of `GET /api/browse/events`. Coordinates come in pairs; with them the list is sorted by distance unless
 * [sort] says otherwise, and [radiusKm] limits it. [from] defaults to now, so only events that have not ended show.
 */
data class EventBrowseQuery(
    @field:DecimalMin("-90.0") @field:DecimalMax("90.0") val lat: Double? = null,
    @field:DecimalMin("-180.0") @field:DecimalMax("180.0") val lon: Double? = null,
    @field:Positive @field:DecimalMax(BrowseLimits.MAX_RADIUS_KM) val radiusKm: Double? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) val from: LocalDateTime? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) val to: LocalDateTime? = null,
    val kind: EventType? = null,
    @field:Size(max = BrowseLimits.MAX_QUERY_LENGTH) val q: String? = null,
    @field:Pattern(regexp = "distance|start") val sort: String? = null,
    @field:Min(0) val page: Int = 0,
    @field:Min(1) @field:Max(BrowseLimits.MAX_PAGE_SIZE) val size: Int = BrowseLimits.DEFAULT_PAGE_SIZE,
)

/** Filters of `GET /api/browse/places`; sorted by distance with coordinates, by name otherwise. */
data class PlaceBrowseQuery(
    @field:DecimalMin("-90.0") @field:DecimalMax("90.0") val lat: Double? = null,
    @field:DecimalMin("-180.0") @field:DecimalMax("180.0") val lon: Double? = null,
    @field:Positive @field:DecimalMax(BrowseLimits.MAX_RADIUS_KM) val radiusKm: Double? = null,
    @field:Size(max = BrowseLimits.MAX_TAG_LENGTH) val tag: String? = null,
    @field:Size(max = BrowseLimits.MAX_QUERY_LENGTH) val q: String? = null,
    @field:Pattern(regexp = "distance|name") val sort: String? = null,
    @field:Min(0) val page: Int = 0,
    @field:Min(1) @field:Max(BrowseLimits.MAX_PAGE_SIZE) val size: Int = BrowseLimits.DEFAULT_PAGE_SIZE,
)

/** Filters of `GET /api/browse/performers`; always sorted by name. */
data class PerformerBrowseQuery(
    @field:Size(max = BrowseLimits.MAX_QUERY_LENGTH) val genre: String? = null,
    @field:Size(max = BrowseLimits.MAX_QUERY_LENGTH) val q: String? = null,
    @field:Pattern(regexp = "name") val sort: String? = null,
    @field:Min(0) val page: Int = 0,
    @field:Min(1) @field:Max(BrowseLimits.MAX_PAGE_SIZE) val size: Int = BrowseLimits.DEFAULT_PAGE_SIZE,
)
