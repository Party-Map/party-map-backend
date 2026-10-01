package com.partymap.backend.domain.browse.dto

import com.partymap.backend.domain.common.dto.GeoPointDto
import com.partymap.backend.domain.event.db.EventType
import java.time.LocalDateTime
import java.util.UUID

/** The venue as an event row shows it. */
data class BrowsePlaceSummaryDto(val id: UUID, val name: String, val city: String, val location: GeoPointDto)

/** One event in the browse list; [image] falls back to the venue's, [distanceKm] needs the caller's coordinates. */
data class BrowseEventItemDto(
    val id: UUID,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val kind: EventType,
    val image: String?,
    val price: String?,
    val place: BrowsePlaceSummaryDto,
    val distanceKm: Double?,
)

data class BrowsePlaceItemDto(
    val id: UUID,
    val name: String,
    val city: String,
    val address: String,
    val image: String?,
    val tags: List<String>,
    val location: GeoPointDto,
    val distanceKm: Double?,
)

data class BrowsePerformerItemDto(val id: UUID, val name: String, val genre: String, val image: String?)

/** One page of a browse list; [page] is zero-based and [total] counts every match of the filters. */
data class BrowseEventsPageDto(val items: List<BrowseEventItemDto>, val total: Long, val page: Int, val size: Int)

data class BrowsePlacesPageDto(val items: List<BrowsePlaceItemDto>, val total: Long, val page: Int, val size: Int)

data class BrowsePerformersPageDto(
    val items: List<BrowsePerformerItemDto>,
    val total: Long,
    val page: Int,
    val size: Int,
)

/** A place tag with the number of places carrying it, most used first. */
data class TagCountDto(val tag: String, val count: Long)

/** A performer genre with the number of performers in it, most common first. */
data class GenreCountDto(val genre: String, val count: Long)
