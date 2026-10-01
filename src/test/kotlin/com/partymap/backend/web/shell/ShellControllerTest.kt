package com.partymap.backend.web.shell

import com.partymap.backend.support.IntegrationTest
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import org.xml.sax.InputSource
import tools.jackson.databind.JsonNode
import java.io.StringReader
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory

class ShellControllerTest : IntegrationTest() {
    private fun page(path: String) = mockMvc.get(path).andReturn().response

    /** The JSON inside `<script type="application/ld+json">` or `<script type="application/json" id="pm-data">`. */
    private fun script(html: String, opening: String): JsonNode {
        val start = html.indexOf(opening)
        assertTrue(start >= 0, "no $opening in the page")
        val body = html.substring(start + opening.length, html.indexOf("</script>", start))
        return jsonMapper.readTree(body)
    }

    private val jsonLdTag = "<script type=\"application/ld+json\">"
    private val dataTag = "<script type=\"application/json\" id=\"pm-data\">"

    @Nested
    inner class Events {
        @Test
        fun `renders the event shell with its metadata, schema graph and the page data the app seeds from`() {
            val place = data.place(name = "Akvárium Klub", image = "https://img.example/akvarium.jpg")
            val performer = data.performer(name = "DJ Pond")
            val event = data.event(place, title = "Pond Party", lineup = listOf(performer))

            val response = mockMvc.get("/events/${event.id}").andExpect {
                status { isOk() }
                content { contentType("text/html;charset=UTF-8") }
                header { string("Cache-Control", "max-age=60, public") }
                header { doesNotExist("X-Robots-Tag") }
            }.andReturn().response
            val html = response.contentAsString

            assertTrue(html.contains("<title>Pond Party · "), "title: $html")
            assertTrue(html.contains(" at Akvárium Klub | PartyMap</title>"))
            assertTrue(html.contains("<meta name=\"description\" content=\"An evening at the pond\" />"))
            assertTrue(html.contains("<link rel=\"canonical\" href=\"http://localhost:3000/events/${event.id}\" />"))
            assertTrue(html.contains("<meta property=\"og:title\" content=\"Pond Party · "))
            assertTrue(html.contains("<meta property=\"og:image\" content=\"https://img.example/akvarium.jpg\" />"))
            assertTrue(html.contains("<meta name=\"twitter:card\" content=\"summary_large_image\" />"))
            assertTrue(html.contains("<h1>Pond Party</h1>"))
            assertTrue(html.contains("<time datetime=\"${iso(event.start)}+0"), "offset time: $html")
            assertTrue(html.contains("<address>"))
            assertTrue(html.contains("<a href=\"http://localhost:3000/performers/${performer.id}\">DJ Pond</a>"))
            assertTrue(html.contains("<script src=\"/static/js/index.js\"></script>"), "the app's bundle is kept")
            assertFalse(html.contains("<!--pm:"), "markers are consumed")

            val graph = script(html, jsonLdTag)
            assertEquals("Event", graph.path("@type").asString())
            assertEquals("Pond Party", graph.path("name").asString())
            assertTrue(graph.path("startDate").asString().startsWith(iso(event.start)))
            assertEquals("https://schema.org/EventScheduled", graph.path("eventStatus").asString())
            assertEquals("Akvárium Klub", graph.path("location").path("name").asString())
            assertEquals(47.4979, graph.path("location").path("geo").path("latitude").asDouble())
            assertEquals("Budapest", graph.path("location").path("address").path("addressLocality").asString())
            assertEquals("DJ Pond", graph.path("performer").get(0).path("name").asString())
            assertEquals("3000", graph.path("offers").path("price").asString())
            assertEquals("HUF", graph.path("offers").path("priceCurrency").asString())

            val preloaded = script(html, dataTag)
            assertEquals(1, preloaded.path("v").asInt())
            assertEquals("event", preloaded.path("kind").asString())
            assertEquals(event.id.toString(), preloaded.path("id").asString())
            val api = jsonMapper.readTree(page("/api/events/${event.id}").contentAsString)
            assertEquals(api, preloaded.path("data").path("event"), "the inlined event is what the API answers")
            assertEquals(place.id.toString(), preloaded.path("data").path("place").path("id").asString())
        }

        @Test
        fun `has no offer for a price that is not a number`() {
            val place = data.place()
            val event = data.event(place)
            jdbc.update("UPDATE event_entity SET price = 'ask at the door' WHERE id = ?", event.id)

            val html = page("/events/${event.id}").contentAsString
            assertFalse(script(html, jsonLdTag).has("offers"))
        }

        @Test
        fun `escapes a title that tries to break out of the scripts and the markup`() {
            val place = data.place()
            val event = data.event(place, title = "</script><script>alert(1)</script>")

            val html = page("/events/${event.id}").contentAsString
            assertFalse(html.contains("<script>alert(1)"), "raw script in: $html")
            assertTrue(html.contains("&lt;/script&gt;&lt;script&gt;alert(1)&lt;/script&gt;"))
            assertTrue(html.contains("\\u003c/script\\u003e\\u003cscript\\u003ealert(1)"))
        }
    }

    @Nested
    inner class PlacesAndPerformers {
        @Test
        fun `renders the place shell with its upcoming events in the graph and every event in the data`() {
            val place = data.place(
                name = "Akvárium Klub",
                description = "Club under the pond",
                image = "https://img.example/a.jpg",
            )
            data.event(place, title = "Future", start = data.now.plusDays(2))
            data.event(place, title = "Past", start = data.now.minusDays(3), end = data.now.minusDays(2))

            val html = mockMvc.get(
                "/places/${place.id}",
            ).andExpect { status { isOk() } }.andReturn().response.contentAsString
            assertTrue(html.contains("<title>Akvárium Klub · Budapest | PartyMap</title>"))
            assertTrue(html.contains("<address><span>Erzsébet tér 12</span>, <span>Budapest</span></address>"))
            assertTrue(html.contains("<li>club</li>"))
            assertTrue(html.contains(">Future</a>"))
            assertFalse(html.contains(">Past</a>"))

            val graph = script(html, jsonLdTag)
            assertEquals("Place", graph.path("@type").asString())
            assertEquals("Erzsébet tér 12", graph.path("address").path("streetAddress").asString())
            assertEquals(1, graph.path("event").size())
            assertEquals("Future", graph.path("event").get(0).path("name").asString())

            val preloaded = script(html, dataTag)
            assertEquals("place", preloaded.path("kind").asString())
            assertEquals(2, preloaded.path("data").path("events").size())
            assertEquals(place.id.toString(), preloaded.path("data").path("place").path("id").asString())
        }

        @Test
        fun `renders the performer shell as a music group with its links`() {
            val performer = data.performer(name = "DJ Pond", genre = "Techno", bio = "Plays until sunrise")
            data.event(data.place(), title = "Pond Party", lineup = listOf(performer))

            val html = mockMvc.get(
                "/performers/${performer.id}",
            ).andExpect { status { isOk() } }.andReturn().response.contentAsString
            assertTrue(html.contains("<title>DJ Pond · Techno | PartyMap</title>"))
            assertTrue(html.contains("<meta name=\"description\" content=\"Plays until sunrise\" />"))
            assertTrue(html.contains("<meta name=\"twitter:card\" content=\"summary\" />"), "no image, no large card")

            val graph = script(html, jsonLdTag)
            assertEquals("MusicGroup", graph.path("@type").asString())
            assertEquals("Techno", graph.path("genre").asString())
            assertEquals("https://instagram.com/djpond", graph.path("sameAs").get(0).asString())
            assertEquals("Pond Party", graph.path("event").get(0).path("name").asString())

            val preloaded = script(html, dataTag)
            assertEquals("performer", preloaded.path("kind").asString())
            assertEquals("DJ Pond", preloaded.path("data").path("performer").path("name").asString())
            assertEquals(1, preloaded.path("data").path("events").size())
        }
    }

    @Nested
    inner class NotFoundAndSitemap {
        @Test
        fun `an unknown or malformed id answers the shell of the 404 page, not indexed`() {
            for (path in listOf(
                "/events/${UUID.randomUUID()}",
                "/places/not-a-uuid",
                "/performers/${UUID.randomUUID()}",
            )) {
                mockMvc.get(path).andExpect {
                    status { isNotFound() }
                    content { contentType("text/html;charset=UTF-8") }
                    header { string("X-Robots-Tag", "noindex") }
                    content { string(containsString("<meta name=\"robots\" content=\"noindex\" />")) }
                    content { string(containsString("<title>Not found | PartyMap</title>")) }
                    content { string(containsString("<h1>Not found</h1>")) }
                    content { string(not(containsString("pm-data"))) }
                    content { string(not(containsString("rel=\"canonical\""))) }
                }
            }
        }

        @Test
        fun `the sitemap lists the lists, places, performers and the events that have not ended`() {
            val owner = data.user()
            val place = data.place(owner)
            val performer = data.performer(owner)
            val future = data.event(place, title = "Future")
            val past = data.event(place, title = "Past", start = data.now.minusDays(3), end = data.now.minusDays(2))

            val xml = mockMvc.get("/sitemap.xml").andExpect {
                status { isOk() }
                content { contentType("application/xml") }
            }.andReturn().response.contentAsString

            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                InputSource(StringReader(xml)),
            )
            val locations = document.getElementsByTagName(
                "loc",
            ).let { nodes -> (0 until nodes.length).map { nodes.item(it).textContent } }
            assertTrue(
                locations.containsAll(
                    listOf(
                        "http://localhost:3000/",
                        "http://localhost:3000/browse/events",
                        "http://localhost:3000/places/${place.id}",
                        "http://localhost:3000/performers/${performer.id}",
                        "http://localhost:3000/events/${future.id}",
                    ),
                ),
                "$locations",
            )
            assertFalse(locations.contains("http://localhost:3000/events/${past.id}"))
            assertTrue(xml.contains("<lastmod>"))
        }
    }
}
