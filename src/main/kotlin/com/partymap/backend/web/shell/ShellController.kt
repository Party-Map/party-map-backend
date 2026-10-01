package com.partymap.backend.web.shell

import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * The HTML routes the frontend's nginx (and the dev server) hand to the backend: the app's own `index.html` with
 * the page's metadata, schema.org graph and data inlined. An unknown or malformed id answers the shell of the
 * app's 404 page with the same status, never a problem document.
 */
@Controller
class ShellController(
    private val pages: ShellPageService,
    private val renderer: ShellRenderer,
    private val sitemap: SitemapService,
) {
    @GetMapping("/events/{id}")
    @ResponseBody
    fun event(@PathVariable id: String): ResponseEntity<String> = html(id, pages::event)

    @GetMapping("/places/{id}")
    @ResponseBody
    fun place(@PathVariable id: String): ResponseEntity<String> = html(id, pages::place)

    @GetMapping("/performers/{id}")
    @ResponseBody
    fun performer(@PathVariable id: String): ResponseEntity<String> = html(id, pages::performer)

    @GetMapping("/sitemap.xml")
    @ResponseBody
    fun sitemap(): ResponseEntity<String> = ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_XML)
        .cacheControl(CACHE)
        .body(sitemap.xml())

    private fun html(id: String, load: (UUID) -> ShellPage?): ResponseEntity<String> {
        val page = runCatching { UUID.fromString(id) }.getOrNull()?.let(load)
        val response = ResponseEntity.status(if (page == null) HttpStatus.NOT_FOUND else HttpStatus.OK)
            .contentType(HTML)
            .cacheControl(CACHE)
        if (page == null) response.header("X-Robots-Tag", "noindex")
        return response.body(renderer.render(page ?: pages.notFound()))
    }

    private companion object {
        val HTML = MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)
        const val MAX_AGE_SECONDS = 60L
        val CACHE: CacheControl = CacheControl.maxAge(MAX_AGE_SECONDS, TimeUnit.SECONDS).cachePublic()
    }
}
