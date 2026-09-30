package com.partymap.backend.domain.event

import com.partymap.backend.config.Roles
import com.partymap.backend.support.IntegrationTest
import com.partymap.backend.support.tokenFor
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import java.util.UUID

class EventControllerTest : IntegrationTest() {
    @Test
    fun `lists every event with its lineup`() {
        val performer = data.performer()
        val event = data.event(data.place(), lineup = listOf(performer))

        mockMvc.get("/api/events").andExpect {
            status { isOk() }
            jsonPath("$", hasSize<Any>(1))
            jsonPath("$[0].id") { value(event.id.toString()) }
            jsonPath("$[0].placeId") { value(event.place.id.toString()) }
            jsonPath("$[0].kind") { value("TECHNO") }
            jsonPath("$[0].start") { value(iso(event.start)) }
            jsonPath("$[0].lineupItems[0].performer.name") { value("DJ Pond") }
            jsonPath("$[0].links[0].type") { value("FACEBOOK") }
        }
    }

    @Test
    fun `filters the events by place, in start order`() {
        val place = data.place()
        data.event(place, title = "Later", start = data.now.plusDays(3))
        data.event(place, title = "Sooner", start = data.now.plusDays(2))
        data.event(data.place(name = "Elsewhere"), title = "Elsewhere")

        mockMvc.get("/api/events?placeId=${place.id}").andExpect {
            status { isOk() }
            jsonPath("$[*].title", contains("Sooner", "Later"))
        }
    }

    @Test
    fun `filters the events by performer`() {
        val performer = data.performer()
        val place = data.place()
        data.event(place, title = "With", lineup = listOf(performer))
        data.event(place, title = "Without")

        mockMvc.get("/api/events?performerId=${performer.id}").andExpect {
            status { isOk() }
            jsonPath("$[*].title", contains("With"))
        }
        mockMvc.get("/api/events?performerId=${UUID.randomUUID()}").andExpect {
            status { isOk() }
            jsonPath("$", hasSize<Any>(0))
        }
    }

    @Test
    fun `returns one event and its place`() {
        val event = data.event(data.place(name = "Dürer Kert"))

        mockMvc.get("/api/events/${event.id}").andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Pond Party") }
            jsonPath("$.price") { value("3000 HUF") }
        }
        mockMvc.get("/api/events/${event.id}/place").andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Dürer Kert") }
        }
    }

    @Test
    fun `an unknown event and its place are not found`() {
        mockMvc.get("/api/events/${UUID.randomUUID()}").andExpect { status { isNotFound() } }
        mockMvc.get("/api/events/${UUID.randomUUID()}/place").andExpect { status { isNotFound() } }
    }

    @Test
    fun `upcoming events are the next event of each place, falling back to the place image`() {
        val owner = data.user()
        val first = data.place(owner, name = "First")
        val second = data.place(owner, name = "Second")
        data.event(first, title = "Past", start = data.now.minusDays(2), end = data.now.minusDays(1))
        data.event(first, title = "Next", start = data.now.plusDays(1))
        data.event(first, title = "After next", start = data.now.plusDays(5))
        data.event(
            second,
            title = "Running",
            start = data.now.minusHours(1),
            end = data.now.plusHours(3),
            image = "https://example.com/running.jpg",
        )

        mockMvc.get("/api/events/upcoming-events").andExpect {
            status { isOk() }
            jsonPath("$[*].title", containsInAnyOrder("Next", "Running"))
            jsonPath("$[?(@.title == 'Next')].placeId") { value(first.id.toString()) }
            jsonPath("$[?(@.title == 'Next')].kind") { value("TECHNO") }
            jsonPath("$[?(@.title == 'Next')].image") { value(null as Any?) }
            jsonPath("$[?(@.title == 'Running')].image") { value("https://example.com/running.jpg") }
        }
    }

    @Test
    fun `liked events need a signed-in user`() {
        mockMvc.get("/api/events/liked-events").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `liked events are split into upcoming and past`() {
        val sub = UUID.randomUUID()
        val place = data.place()
        val past = data.event(place, title = "Past", start = data.now.minusDays(3), end = data.now.minusDays(2))
        val older = data.event(place, title = "Older", start = data.now.minusDays(9), end = data.now.minusDays(8))
        val soon = data.event(place, title = "Soon", start = data.now.plusDays(1))
        val later = data.event(place, title = "Later", start = data.now.plusDays(4))
        data.event(place, title = "Not liked")
        listOf(past, older, soon, later).forEach {
            mockMvc.put("/api/me/likes/events/${it.id}") { with(tokenFor(sub)) }
        }

        mockMvc.get("/api/events/liked-events") { with(tokenFor(sub)) }.andExpect {
            status { isOk() }
            jsonPath("$.upcoming[*].title", contains("Soon", "Later"))
            jsonPath("$.past[*].title", contains("Past", "Older"))
        }
    }

    @Test
    fun `owned events need the organizer role and list only the caller's events`() {
        val owner = data.user()
        data.event(data.place(name = "Venue"), owner = owner, title = "Mine")
        data.event(data.place(), title = "Theirs")

        mockMvc.get("/api/events/owned-events") { with(tokenFor(owner.sub, Roles.PLACE_MANAGER)) }
            .andExpect { status { isForbidden() } }
        mockMvc.get("/api/events/owned-events") { with(tokenFor(owner.sub, Roles.EVENT_ORGANIZER)) }.andExpect {
            status { isOk() }
            jsonPath("$[*].title", contains("Mine"))
            jsonPath("$[0].placeName") { value("Venue") }
        }
    }
}
