package com.partymap.backend.web.shell

import com.partymap.backend.domain.common.exception.NotFoundException
import com.partymap.backend.domain.event.EventService
import com.partymap.backend.domain.event.dto.EventDto
import com.partymap.backend.domain.performer.PerformerService
import com.partymap.backend.domain.place.PlaceService
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

/**
 * Builds the shell of each page from the same services the API uses, so the inlined data is exactly what the
 * app's page hooks would fetch (`{ event, place }`, `{ place, events }`, `{ performer, events }`). No transaction of
 * its own: each service call has one, and a "not found" thrown inside a shared transaction would mark it
 * rollback-only even though the shell answers it with its own 404 page.
 */
@Service
class ShellPageService(
    private val eventService: EventService,
    private val placeService: PlaceService,
    private val performerService: PerformerService,
    private val properties: ShellProperties,
    private val clock: Clock,
) {
    fun event(id: UUID): ShellPage? = found {
        val event = eventService.get(id)
        val place = eventService.placeOf(id)
        val base = properties.baseUrl
        val zone = properties.zoneId
        ShellPage(
            meta = ShellMeta(
                title = SeoText.title(event.title, "${SeoText.dayMonth(event.start)} at ${place.name}"),
                description = SeoText.description(event.description, "${event.title} at ${place.name}, ${place.city}."),
                canonical = "$base/events/$id",
                image = event.image ?: place.image,
            ),
            view = "event",
            model = mapOf(
                "title" to event.title,
                "image" to (event.image ?: place.image),
                "startIso" to SeoText.offset(event.start, zone),
                "whenText" to SeoText.range(event.start, event.end),
                "placeName" to place.name,
                "placeCity" to place.city,
                "placeUrl" to "$base/places/${place.id}",
                "description" to event.description,
                "price" to event.price,
                "lineup" to event.lineupItems.map {
                    mapOf("name" to it.performer.name, "url" to "$base/performers/${it.performer.id}")
                },
            ),
            jsonLd = JsonLd.event(event, place, base, zone),
            data = preloaded("event", id, mapOf("event" to event, "place" to place)),
        )
    }

    fun place(id: UUID): ShellPage? = found {
        val place = placeService.get(id)
        val events = eventService.list(placeId = id, performerId = null)
        val base = properties.baseUrl
        val zone = properties.zoneId
        ShellPage(
            meta = ShellMeta(
                title = SeoText.title(place.name, place.city),
                description = SeoText.description(place.description, "${place.name}, ${place.address}, ${place.city}."),
                canonical = "$base/places/$id",
                image = place.image,
            ),
            view = "place",
            model = mapOf(
                "name" to place.name,
                "image" to place.image,
                "address" to place.address,
                "city" to place.city,
                "description" to place.description,
                "tags" to place.tags,
                "events" to upcoming(events).map { eventRow(it, base, zone) },
            ),
            jsonLd = JsonLd.place(place, upcoming(events), base, zone),
            data = preloaded("place", id, mapOf("place" to place, "events" to events)),
        )
    }

    fun performer(id: UUID): ShellPage? = found {
        val performer = performerService.get(id)
        val events = eventService.list(placeId = null, performerId = id)
        val base = properties.baseUrl
        val zone = properties.zoneId
        ShellPage(
            meta = ShellMeta(
                title = SeoText.title(performer.name, performer.genre),
                description = SeoText.description(performer.bio, "${performer.name}, ${performer.genre}."),
                canonical = "$base/performers/$id",
                image = performer.image,
            ),
            view = "performer",
            model = mapOf(
                "name" to performer.name,
                "genre" to performer.genre,
                "image" to performer.image,
                "bio" to performer.bio,
                "events" to upcoming(events).map { eventRow(it, base, zone) },
            ),
            jsonLd = JsonLd.performer(performer, upcoming(events), base, zone),
            data = preloaded("performer", id, mapOf("performer" to performer, "events" to events)),
        )
    }

    /** The shell of an unknown page: not indexed, no data; the app shows its own 404. */
    fun notFound(): ShellPage = ShellPage(
        meta = ShellMeta(
            title = SeoText.title("Not found"),
            description = "This page does not exist.",
            canonical = null,
            image = null,
            noindex = true,
        ),
        view = "not-found",
        model = emptyMap(),
    )

    private fun upcoming(events: List<EventDto>): List<EventDto> {
        val now = LocalDateTime.now(clock)
        return events.filter { it.end.isAfter(now) }.sortedBy { it.start }
    }

    private fun eventRow(event: EventDto, base: String, zone: java.time.ZoneId): Map<String, Any?> = mapOf(
        "title" to event.title,
        "url" to "$base/events/${event.id}",
        "startIso" to SeoText.offset(event.start, zone),
        "whenText" to SeoText.range(event.start, event.end),
    )

    private fun preloaded(kind: String, id: UUID, data: Map<String, Any?>): Map<String, Any?> =
        mapOf("v" to PRELOAD_VERSION, "kind" to kind, "id" to id.toString(), "data" to data)

    private inline fun found(build: () -> ShellPage): ShellPage? = try {
        build()
    } catch (_: NotFoundException) {
        null
    }

    private companion object {
        /** Bumped when the inlined data's shape changes, so an older app ignores it. */
        const val PRELOAD_VERSION = 1
    }
}
