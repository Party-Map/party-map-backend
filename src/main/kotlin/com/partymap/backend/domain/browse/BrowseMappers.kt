package com.partymap.backend.domain.browse

import com.partymap.backend.domain.browse.dto.BrowseEventItemDto
import com.partymap.backend.domain.browse.dto.BrowsePerformerItemDto
import com.partymap.backend.domain.browse.dto.BrowsePlaceItemDto
import com.partymap.backend.domain.browse.dto.BrowsePlaceSummaryDto
import com.partymap.backend.domain.browse.dto.GenreCountDto
import com.partymap.backend.domain.browse.dto.TagCountDto
import com.partymap.backend.domain.common.dto.GeoPointDto
import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.performer.db.GenreCount
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.place.db.TagCount
import kotlin.math.roundToLong

fun EventEntity.toBrowseItem(distanceKm: Double?): BrowseEventItemDto = BrowseEventItemDto(
    id = requiredId,
    title = title,
    start = start,
    end = end,
    kind = kind,
    image = image ?: place.image,
    price = price,
    place = place.toBrowseSummary(),
    distanceKm = distanceKm?.toMetrePrecision(),
)

fun PlaceEntity.toBrowseSummary(): BrowsePlaceSummaryDto = BrowsePlaceSummaryDto(
    id = requiredId,
    name = name,
    city = city,
    location = GeoPointDto(location.latitude, location.longitude),
)

fun PlaceEntity.toBrowseItem(distanceKm: Double?): BrowsePlaceItemDto = BrowsePlaceItemDto(
    id = requiredId,
    name = name,
    city = city,
    address = address,
    image = image,
    tags = tags.sorted(),
    location = GeoPointDto(location.latitude, location.longitude),
    distanceKm = distanceKm?.toMetrePrecision(),
)

fun PerformerEntity.toBrowseItem(): BrowsePerformerItemDto = BrowsePerformerItemDto(
    id = requiredId,
    name = name,
    genre = genre,
    image = image,
)

fun TagCount.toDto(): TagCountDto = TagCountDto(tag = tag, count = count)

fun GenreCount.toDto(): GenreCountDto = GenreCountDto(genre = genre, count = count)

/** Kilometres rounded to the metre: the formula's rounding noise (centimetres at the origin) never reaches the API. */
private fun Double.toMetrePrecision(): Double = (this * METRES_PER_KM).roundToLong() / METRES_PER_KM

private const val METRES_PER_KM = 1000.0
