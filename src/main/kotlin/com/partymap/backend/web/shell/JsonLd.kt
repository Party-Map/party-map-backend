package com.partymap.backend.web.shell

import com.partymap.backend.domain.event.dto.EventDto
import com.partymap.backend.domain.performer.dto.PerformerDto
import com.partymap.backend.domain.place.dto.PlaceDto
import java.time.ZoneId

/** schema.org graphs for the three page kinds, as maps the JSON mapper writes (null and empty values left out). */
object JsonLd {
    private const val CONTEXT = "https://schema.org"

    fun event(event: EventDto, place: PlaceDto, base: String, zone: ZoneId): Map<String, Any?> = node(
        "@context" to CONTEXT,
        "@type" to "Event",
        "name" to event.title,
        "startDate" to SeoText.offset(event.start, zone),
        "endDate" to SeoText.offset(event.end, zone),
        "eventStatus" to "https://schema.org/EventScheduled",
        "eventAttendanceMode" to "https://schema.org/OfflineEventAttendanceMode",
        "url" to "$base/events/${event.id}",
        "image" to (event.image ?: place.image),
        "description" to event.description,
        "location" to placeNode(place, base),
        "performer" to event.lineupItems.map { item ->
            node(
                "@type" to "MusicGroup",
                "name" to item.performer.name,
                "url" to "$base/performers/${item.performer.id}",
            )
        },
        "offers" to offer(event.price, "$base/events/${event.id}"),
    )

    fun place(place: PlaceDto, upcoming: List<EventDto>, base: String, zone: ZoneId): Map<String, Any?> =
        placeNode(place, base) + node(
            "@context" to CONTEXT,
            "description" to place.description,
            "image" to place.image,
            "event" to upcoming.map { eventSummary(it, base, zone) },
        )

    fun performer(performer: PerformerDto, upcoming: List<EventDto>, base: String, zone: ZoneId): Map<String, Any?> =
        node(
            "@context" to CONTEXT,
            "@type" to "MusicGroup",
            "name" to performer.name,
            "genre" to performer.genre,
            "url" to "$base/performers/${performer.id}",
            "image" to performer.image,
            "description" to performer.bio,
            "sameAs" to performer.links.map { it.url },
            "event" to upcoming.map { eventSummary(it, base, zone) },
        )

    /** An `Offer` when the free-text price is a number (or "free"); nothing for "ask at the door" and the like. */
    internal fun offer(price: String?, url: String): Map<String, Any?>? {
        val (amount, currency) = parsePrice(price) ?: return null
        return node(
            "@type" to "Offer",
            "price" to amount,
            "priceCurrency" to currency,
            "url" to url,
            "availability" to "https://schema.org/InStock",
        )
    }

    /** "Free entry" → 0 HUF; "3000 HUF", "4,500 Ft", "€15" → the digits and the currency they name (HUF by default). */
    internal fun parsePrice(raw: String?): Pair<String, String>? {
        val text = raw?.trim().orEmpty()
        val digits = AMOUNT.find(text)?.value?.replace(SEPARATORS, "")
        return when {
            text.isEmpty() -> null
            FREE.containsMatchIn(text) -> "0" to "HUF"
            digits == null -> null
            else -> digits to currencyOf(text)
        }
    }

    private fun currencyOf(text: String): String {
        val lower = text.lowercase()
        return when {
            '€' in text || "eur" in lower -> "EUR"
            '$' in text || "usd" in lower -> "USD"
            '£' in text || "gbp" in lower -> "GBP"
            else -> "HUF"
        }
    }

    private val AMOUNT = Regex("\\d[\\d\\s,]*(?:\\.\\d+)?")
    private val SEPARATORS = Regex("[\\s,]")
    private val FREE = Regex("\\b(free|ingyenes)\\b", RegexOption.IGNORE_CASE)

    private fun placeNode(place: PlaceDto, base: String): Map<String, Any?> = node(
        "@type" to "Place",
        "name" to place.name,
        "url" to "$base/places/${place.id}",
        "address" to node(
            "@type" to "PostalAddress",
            "streetAddress" to place.address,
            "addressLocality" to place.city,
            "addressCountry" to "HU",
        ),
        "geo" to node(
            "@type" to "GeoCoordinates",
            "latitude" to place.location.latitude,
            "longitude" to place.location.longitude,
        ),
    )

    private fun eventSummary(event: EventDto, base: String, zone: ZoneId): Map<String, Any?> = node(
        "@type" to "Event",
        "name" to event.title,
        "startDate" to SeoText.offset(event.start, zone),
        "endDate" to SeoText.offset(event.end, zone),
        "url" to "$base/events/${event.id}",
    )

    private fun node(vararg entries: Pair<String, Any?>): Map<String, Any?> = linkedMapOf(*entries).filterValues {
        it != null && (it !is Collection<*> || it.isNotEmpty()) && (it !is String || it.isNotBlank())
    }
}
