package com.partymap.backend.web.shell

import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.place.db.PlaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime

/** `/sitemap.xml`: the map, the browse lists, every place and performer, and the events that have not ended. */
@Service
@Transactional(readOnly = true)
class SitemapService(
    private val placeRepository: PlaceRepository,
    private val performerRepository: PerformerRepository,
    private val eventRepository: EventRepository,
    private val properties: ShellProperties,
    private val clock: Clock,
) {
    fun xml(): String {
        val base = properties.baseUrl
        val out = StringBuilder()
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        out.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n")
        listOf("/", "/browse/events", "/browse/places", "/browse/performers").forEach { url(out, "$base$it", null) }
        placeRepository.findAll().forEach { url(out, "$base/places/${it.requiredId}", it.updatedAt) }
        performerRepository.findAll().forEach { url(out, "$base/performers/${it.requiredId}", it.updatedAt) }
        eventRepository.findAllByEndAfterOrderByPlaceIdAscStartAsc(LocalDateTime.now(clock))
            .forEach { url(out, "$base/events/${it.requiredId}", it.updatedAt) }
        out.append("</urlset>\n")
        return out.toString()
    }

    private fun url(out: StringBuilder, loc: String, lastModified: Instant?) {
        out.append("  <url><loc>").append(escape(loc)).append("</loc>")
        lastModified?.let {
            out.append(
                "<lastmod>",
            ).append(it.atZone(properties.zoneId).toLocalDate()).append("</lastmod>")
        }
        out.append("</url>\n")
    }

    private fun escape(text: String): String = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}
