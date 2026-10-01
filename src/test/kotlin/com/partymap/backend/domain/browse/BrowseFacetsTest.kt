package com.partymap.backend.domain.browse

import com.partymap.backend.support.IntegrationTest
import org.hamcrest.Matchers.contains
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class BrowseFacetsTest : IntegrationTest() {
    @Test
    fun `counts places per tag, most used first`() {
        val owner = data.user()
        data.place(owner, name = "A", tags = setOf("techno", "club"))
        data.place(owner, name = "B", tags = setOf("techno", "bar"))
        data.place(owner, name = "C", tags = setOf("techno"))

        mockMvc.get("/api/browse/place-tags").andExpect {
            status { isOk() }
            jsonPath("$[*].tag", contains("techno", "bar", "club"))
            jsonPath("$[0].count") { value(3) }
            jsonPath("$[1].count") { value(1) }
        }
    }

    @Test
    fun `counts performers per genre, most common first`() {
        val owner = data.user()
        data.performer(owner, name = "A", genre = "House")
        data.performer(owner, name = "B", genre = "Techno")
        data.performer(owner, name = "C", genre = "Techno")

        mockMvc.get("/api/browse/performer-genres").andExpect {
            status { isOk() }
            jsonPath("$[*].genre", contains("Techno", "House"))
            jsonPath("$[0].count") { value(2) }
        }
    }
}
