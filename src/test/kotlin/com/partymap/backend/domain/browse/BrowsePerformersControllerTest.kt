package com.partymap.backend.domain.browse

import com.partymap.backend.support.IntegrationTest
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get

class BrowsePerformersControllerTest : IntegrationTest() {
    private fun browse(query: String = "") = mockMvc.get("/api/browse/performers?$query")

    @Test
    fun `lists performers by name`() {
        val owner = data.user()
        data.performer(owner, name = "Klang Duo", genre = "House")
        data.performer(owner, name = "DJ Aurora", genre = "Melodic Techno")

        browse().andExpect {
            status { isOk() }
            jsonPath("$.total") { value(2) }
            jsonPath("$.items[*].name", contains("DJ Aurora", "Klang Duo"))
            jsonPath("$.items[0].genre") { value("Melodic Techno") }
            jsonPath("$.items[0].image") { doesNotExist() }
        }
    }

    @Test
    fun `filters by genre ignoring case and by keywords`() {
        val owner = data.user()
        data.performer(owner, name = "Klang Duo", genre = "House")
        data.performer(owner, name = "DJ Aurora", genre = "Melodic Techno", bio = "Melodic journeys")

        browse("genre=house").andExpect { jsonPath("$.items[*].name", contains("Klang Duo")) }
        browse("q=journeys").andExpect { jsonPath("$.items[*].name", contains("DJ Aurora")) }
        browse("genre=house&q=aurora").andExpect { jsonPath("$.items", hasSize<Any>(0)) }
    }

    @Test
    fun `pages the results and rejects unknown sorts`() {
        val owner = data.user()
        listOf("A", "B", "C").forEach { data.performer(owner, name = it) }

        browse("page=1&size=2&sort=name").andExpect {
            status { isOk() }
            jsonPath("$.total") { value(3) }
            jsonPath("$.page") { value(1) }
            jsonPath("$.items[*].name", contains("C"))
        }
        browse("sort=genre").andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
        }
    }
}
