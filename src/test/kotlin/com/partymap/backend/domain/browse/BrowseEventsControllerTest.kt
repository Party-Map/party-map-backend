package com.partymap.backend.domain.browse

import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.support.IntegrationTest
import org.hamcrest.Matchers.closeTo
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.lessThan
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get

class BrowseEventsControllerTest : IntegrationTest() {
    private val budapest = "lat=47.4979&lon=19.0402"

    private fun browse(query: String = "") = mockMvc.get("/api/browse/events?$query")

    /** Budapest to Szeged is about 162 km. */
    private fun aboutSzegedAway() = closeTo(162.0, 2.0)

    @Nested
    inner class Listing {
        @Test
        fun `lists events that have not ended, soonest first, with the venue and the venue's image`() {
            val place = data.place(name = "Akvárium Klub", image = "https://img.example/akvarium.jpg")
            data.event(place, title = "Ended", start = data.now.minusDays(3), end = data.now.minusDays(2))
            data.event(place, title = "Running", start = data.now.minusHours(1), end = data.now.plusHours(3))
            data.event(place, title = "Tomorrow", image = "https://img.example/tomorrow.jpg")

            browse().andExpect {
                status { isOk() }
                jsonPath("$.total") { value(2) }
                jsonPath("$.page") { value(0) }
                jsonPath("$.size") { value(20) }
                jsonPath("$.items[*].title", contains("Running", "Tomorrow"))
                jsonPath("$.items[0].image") { value("https://img.example/akvarium.jpg") }
                jsonPath("$.items[1].image") { value("https://img.example/tomorrow.jpg") }
                jsonPath("$.items[0].place.id") { value(place.id.toString()) }
                jsonPath("$.items[0].place.name") { value("Akvárium Klub") }
                jsonPath("$.items[0].place.city") { value("Budapest") }
                jsonPath("$.items[0].place.location.latitude") { value(47.4979) }
                jsonPath("$.items[0].kind") { value("TECHNO") }
                jsonPath("$.items[0].price") { value("3000 HUF") }
                jsonPath("$.items[0].distanceKm") { doesNotExist() }
            }
        }

        @Test
        fun `coordinates sort by distance and report it`() {
            val owner = data.user()
            val szeged = data.place(owner, name = "Szeged Riverside", latitude = 46.253, longitude = 20.1414)
            val budapestClub = data.place(owner, name = "Akvárium Klub")
            data.event(szeged, title = "Tisza Ride", start = data.now.plusDays(1))
            data.event(budapestClub, title = "Pond Party", start = data.now.plusDays(2))

            browse(budapest).andExpect {
                status { isOk() }
                jsonPath("$.items[*].title", contains("Pond Party", "Tisza Ride"))
                jsonPath("$.items[0].distanceKm", lessThan(0.01))
                jsonPath("$.items[1].distanceKm", aboutSzegedAway())
            }
        }

        @Test
        fun `a radius keeps only the events near the caller and counts only those`() {
            val owner = data.user()
            data.event(data.place(owner, name = "Szeged", latitude = 46.253, longitude = 20.1414), title = "Far")
            data.event(data.place(owner, name = "Budapest"), title = "Near")

            browse("$budapest&radiusKm=50").andExpect {
                status { isOk() }
                jsonPath("$.total") { value(1) }
                jsonPath("$.items[*].title", contains("Near"))
            }
        }

        @Test
        fun `an event at the caller's own coordinates has distance zero`() {
            data.event(data.place(latitude = 47.4979, longitude = 19.0402), title = "Here")

            browse("$budapest&radiusKm=1").andExpect {
                status { isOk() }
                jsonPath("$.items[0].title") { value("Here") }
                jsonPath("$.items[0].distanceKm") { value(0.0) }
            }
        }

        @Test
        fun `sort=start keeps the start order even with coordinates`() {
            val owner = data.user()
            data.event(data.place(owner, latitude = 46.253, longitude = 20.1414), title = "Far but first")
            data.event(data.place(owner), title = "Near but later", start = data.now.plusDays(2))

            browse("$budapest&sort=start").andExpect {
                status { isOk() }
                jsonPath("$.items[*].title", contains("Far but first", "Near but later"))
                jsonPath("$.items[0].distanceKm", aboutSzegedAway())
            }
        }
    }

    @Nested
    inner class Filtering {
        @Test
        fun `filters by kind`() {
            val place = data.place()
            data.event(place, title = "Jazz Night", kind = EventType.JAZZ)
            data.event(place, title = "Techno Night", kind = EventType.TECHNO)

            browse("kind=JAZZ").andExpect {
                status { isOk() }
                jsonPath("$.items[*].title", contains("Jazz Night"))
            }
        }

        @Test
        fun `every keyword must match a field and LIKE wildcards are literal`() {
            val owner = data.user()
            val szeged = data.place(owner, name = "Szeged Riverside", city = "Szeged")
            data.event(szeged, title = "Tisza Ride")
            data.event(data.place(owner, name = "Akvárium"), title = "Pond Party")

            browse("q=riverside").andExpect { jsonPath("$.items[*].title", contains("Tisza Ride")) }
            browse("q=szeged ride").andExpect { jsonPath("$.items[*].title", contains("Tisza Ride")) }
            browse("q=szeged party").andExpect { jsonPath("$.items", hasSize<Any>(0)) }
            mockMvc.get("/api/browse/events") { param("q", "%") }.andExpect { jsonPath("$.items", hasSize<Any>(0)) }
        }

        @Test
        fun `a time window keeps the events overlapping it`() {
            val place = data.place()
            data.event(place, title = "Too early", start = data.now.plusDays(1))
            data.event(place, title = "In window", start = data.now.plusDays(2))
            data.event(place, title = "Too late", start = data.now.plusDays(5))

            browse("from=${iso(data.now.plusDays(1).plusHours(12))}&to=${iso(data.now.plusDays(3))}").andExpect {
                status { isOk() }
                jsonPath("$.items[*].title", contains("In window"))
            }
        }

        @Test
        fun `pages the results`() {
            val place = data.place()
            data.event(place, title = "First", start = data.now.plusDays(1))
            data.event(place, title = "Second", start = data.now.plusDays(2))
            data.event(place, title = "Third", start = data.now.plusDays(3))

            browse("page=1&size=2").andExpect {
                status { isOk() }
                jsonPath("$.total") { value(3) }
                jsonPath("$.page") { value(1) }
                jsonPath("$.size") { value(2) }
                jsonPath("$.items[*].title", contains("Third"))
            }
        }
    }

    @Nested
    inner class Validation {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "lat=47.5",
                "sort=distance",
                "radiusKm=10",
                "lat=91&lon=19",
                "lat=47&lon=19&radiusKm=0",
                "size=51",
                "size=0",
                "page=-1",
                "sort=soonest",
                "kind=POLKA",
                "from=not-a-date",
            ],
        )
        fun `rejects inconsistent or out-of-range filters`(query: String) {
            browse(query).andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            }
        }

        @Test
        fun `rejects a window that ends before it starts`() {
            browse("from=${iso(data.now.plusDays(2))}&to=${iso(data.now.plusDays(1))}").andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                jsonPath("$.detail") { value("to must not be before from.") }
            }
        }
    }
}
