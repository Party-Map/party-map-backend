package com.partymap.backend.domain.browse

import com.partymap.backend.support.IntegrationTest
import org.hamcrest.Matchers.closeTo
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get

class BrowsePlacesControllerTest : IntegrationTest() {
    private val budapest = "lat=47.4979&lon=19.0402"

    private fun browse(query: String = "") = mockMvc.get("/api/browse/places?$query")

    @Test
    fun `lists places by name with their tags and location`() {
        val owner = data.user()
        data.place(owner, name = "Zebra", tags = setOf("club", "Techno"), image = "https://img.example/z.jpg")
        data.place(owner, name = "akvárium")

        browse().andExpect {
            status { isOk() }
            jsonPath("$.total") { value(2) }
            jsonPath("$.items[*].name", contains("akvárium", "Zebra"))
            jsonPath("$.items[1].tags", contains("Techno", "club"))
            jsonPath("$.items[1].image") { value("https://img.example/z.jpg") }
            jsonPath("$.items[1].address") { value("Erzsébet tér 12") }
            jsonPath("$.items[1].location.longitude") { value(19.0402) }
            jsonPath("$.items[1].distanceKm") { doesNotExist() }
        }
    }

    @Test
    fun `coordinates sort by distance, a radius limits the list`() {
        val owner = data.user()
        data.place(owner, name = "Szeged Riverside", latitude = 46.253, longitude = 20.1414)
        data.place(owner, name = "Akvárium Klub")

        browse(budapest).andExpect {
            status { isOk() }
            jsonPath("$.items[*].name", contains("Akvárium Klub", "Szeged Riverside"))
            jsonPath("$.items[1].distanceKm", closeTo(162.0, 2.0))
        }
        browse("$budapest&radiusKm=100").andExpect {
            jsonPath("$.total") { value(1) }
            jsonPath("$.items[*].name", contains("Akvárium Klub"))
        }
        browse("$budapest&sort=name").andExpect {
            jsonPath("$.items[*].name", contains("Akvárium Klub", "Szeged Riverside"))
        }
    }

    @Test
    fun `filters by tag ignoring case and by keywords`() {
        val owner = data.user()
        data.place(owner, name = "Instant Fogas", tags = setOf("Ruin", "garden"))
        data.place(owner, name = "Szimpla", city = "Szeged", tags = setOf("pub"))

        browse("tag=ruin").andExpect { jsonPath("$.items[*].name", contains("Instant Fogas")) }
        browse("tag=RUIN").andExpect { jsonPath("$.items[*].name", contains("Instant Fogas")) }
        browse("q=szeged").andExpect { jsonPath("$.items[*].name", contains("Szimpla")) }
        browse("q=garden szeged").andExpect { jsonPath("$.items", hasSize<Any>(0)) }
        browse("tag=pub&q=instant").andExpect { jsonPath("$.items", hasSize<Any>(0)) }
    }

    @Test
    fun `pages the results`() {
        val owner = data.user()
        listOf("A", "B", "C").forEach { data.place(owner, name = it) }

        browse("page=1&size=2").andExpect {
            status { isOk() }
            jsonPath("$.total") { value(3) }
            jsonPath("$.items[*].name", contains("C"))
        }
    }

    @Test
    fun `rejects coordinates without their pair and distance filters without coordinates`() {
        browse("lon=19").andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.detail") { value("lat and lon must be given together.") }
        }
        browse("radiusKm=5").andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("radiusKm needs lat and lon.") }
        }
        browse("sort=distance").andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("Sorting by distance needs lat and lon.") }
        }
        browse("sort=city").andExpect { status { isBadRequest() } }
    }
}
