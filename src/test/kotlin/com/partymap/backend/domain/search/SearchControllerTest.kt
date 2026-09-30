package com.partymap.backend.domain.search

import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.support.IntegrationTest
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class SearchControllerTest : IntegrationTest() {
    private fun titles(query: String) = mockMvc.get("/api/search") { param("q", query) }

    @Test
    fun `a blank query finds nothing`() {
        data.place()
        titles("   ").andExpect {
            status { isOk() }
            jsonPath("$.query") { value("") }
            jsonPath("$.hits", hasSize<Any>(0))
        }
    }

    @Test
    fun `finds places by name, city, address, description and tags, each once`() {
        val owner = data.user()
        data.place(owner, name = "Instant Fogas", tags = setOf("ruin", "bar", "garden"))
        data.place(owner, name = "Szimpla", city = "Szeged", tags = setOf("pub"))

        titles("instant").andExpect {
            jsonPath("$.hits[*].title", containsInAnyOrder("Instant Fogas"))
            jsonPath("$.hits[0].type") { value("PLACE") }
            jsonPath("$.hits[0].subtitle") { value("Budapest • Erzsébet tér 12") }
            jsonPath("$.hits[0].placeId") { exists() }
        }
        titles("a").andExpect { jsonPath("$.hits", hasSize<Any>(2)) }
        data.place(owner, name = "No Address", address = " ")
        titles("no address").andExpect { jsonPath("$.hits[0].subtitle") { value("Budapest") } }
        titles("szeged").andExpect { jsonPath("$.hits[*].title", containsInAnyOrder("Szimpla")) }
        titles("RUIN").andExpect { jsonPath("$.hits[*].title", containsInAnyOrder("Instant Fogas")) }
    }

    @Test
    fun `every keyword must match`() {
        val owner = data.user()
        data.place(owner, name = "Dürer Kert", city = "Budapest")
        data.place(owner, name = "Dürer Terasz", city = "Debrecen")

        titles("dürer debrecen").andExpect { jsonPath("$.hits[*].title", containsInAnyOrder("Dürer Terasz")) }
    }

    @Test
    fun `finds upcoming events by title, kind and place, but not past ones`() {
        val place = data.place(name = "Barba Negra", city = "Budapest")
        data.event(place, title = "Jazz Night", kind = EventType.JAZZ)
        data.event(place, title = "Old Jazz", start = data.now.minusDays(3), end = data.now.minusDays(2))

        titles("jazz").andExpect {
            jsonPath("$.hits[*].title", containsInAnyOrder("Jazz Night"))
            jsonPath("$.hits[0].type") { value("EVENT") }
            jsonPath("$.hits[0].subtitle") { value("Barba Negra • Budapest") }
            jsonPath("$.hits[0].nextEventStart") { exists() }
        }
        titles("negra night").andExpect {
            jsonPath("$.hits[?(@.type == 'EVENT')].title", containsInAnyOrder("Jazz Night"))
        }
    }

    @Test
    fun `finds performers by name, genre and bio`() {
        data.performer(name = "Kistehén", genre = "Indie", bio = "Budapest band")

        titles("indie").andExpect {
            jsonPath("$.hits[*].title", containsInAnyOrder("Kistehén"))
            jsonPath("$.hits[0].type") { value("PERFORMER") }
            jsonPath("$.hits[0].subtitle") { value("Indie") }
        }
    }

    @Test
    fun `LIKE wildcards in the query are matched literally`() {
        data.place(name = "Plain")
        data.place(name = "100% Club")
        data.place(name = "Under_score")

        titles("%").andExpect { jsonPath("$.hits[*].title", containsInAnyOrder("100% Club")) }
        titles("_").andExpect { jsonPath("$.hits[*].title", containsInAnyOrder("Under_score")) }
        titles("\\").andExpect { jsonPath("$.hits", hasSize<Any>(0)) }
    }

    @Test
    fun `the query parameter is required`() {
        mockMvc.get("/api/search").andExpect { status { isBadRequest() } }
    }
}
