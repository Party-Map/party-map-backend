package com.partymap.backend.web.shell

import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

/** Where the SPA's `index.html` (the document the shells are spliced into) comes from. */
fun interface ShellTemplateSource {
    fun template(): String
}

/**
 * Fetches the template from the frontend and keeps it for the configured TTL. A failed fetch keeps the last good
 * copy; before any copy exists, [fallback] (a page with the markers but without the app) stands in, so a page's
 * metadata is served even while the frontend is down.
 */
class HttpShellTemplateSource(
    private val client: RestClient,
    private val properties: ShellProperties,
    private val clock: Clock,
    private val fallback: String,
) : ShellTemplateSource {
    private data class Cached(val html: String, val fetchedAt: Instant)

    private val cached = AtomicReference<Cached?>(null)

    override fun template(): String {
        val now = clock.instant()
        val current = cached.get()
        if (current != null && Duration.between(current.fetchedAt, now) < properties.templateTtl) return current.html
        val html = fetch() ?: current?.html ?: fallback
        cached.set(Cached(html, now))
        return html
    }

    private fun fetch(): String? = try {
        client.get().uri(properties.templateUrl).retrieve().body(String::class.java)?.takeIf { it.isNotBlank() }
            ?: run {
                log.warn("The shell template at {} is empty", properties.templateUrl)
                null
            }
    } catch (ex: RestClientException) {
        log.warn("Could not fetch the shell template from {}: {}", properties.templateUrl, ex.message)
        null
    }

    private companion object {
        val log = LoggerFactory.getLogger(HttpShellTemplateSource::class.java)!!
    }
}
