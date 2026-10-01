package com.partymap.backend.web.shell

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The small text rules of the shells: descriptions, titles and the zone-less times as crawlers want them. */
object SeoText {
    const val SITE = "PartyMap"
    const val MAX_DESCRIPTION = 160

    private val DAY_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
    private val LONG: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.ENGLISH)

    /** Whitespace collapsed and cut at a word boundary to fit a search snippet; [fallback] when blank. */
    fun description(text: String?, fallback: String): String {
        val collapsed = text.orEmpty().replace(Regex("\\s+"), " ").trim()
        val source = collapsed.ifEmpty { fallback }
        if (source.length <= MAX_DESCRIPTION) return source
        val cut = source.substring(0, MAX_DESCRIPTION - 1)
        val space = cut.lastIndexOf(' ')
        val kept = if (space > MAX_DESCRIPTION / 2) cut.substring(0, space) else cut
        return kept.trimEnd() + "…"
    }

    fun title(vararg parts: String): String = parts.filter { it.isNotBlank() }.joinToString(" · ") + " | $SITE"

    /** The zone-less time as an ISO 8601 offset date-time in [zone] (`2030-06-01T20:00:00+02:00`). */
    fun offset(dateTime: LocalDateTime, zone: ZoneId): String =
        dateTime.atZone(zone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    fun dayMonth(dateTime: LocalDateTime): String = dateTime.format(DAY_MONTH)

    /** "1 June 2030, 20:00 – 2 June 2030, 04:00" */
    fun range(start: LocalDateTime, end: LocalDateTime): String = "${start.format(LONG)} – ${end.format(LONG)}"
}
