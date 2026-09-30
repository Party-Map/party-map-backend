package com.partymap.backend.domain.like

import com.partymap.backend.support.IntegrationTest
import com.partymap.backend.support.tokenFor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class LikeControllerTest : IntegrationTest() {
    private fun target(kind: String): UUID = when (kind) {
        "events" -> data.event(data.place()).id!!
        "places" -> data.place().id!!
        else -> data.performer().id!!
    }

    private fun likeTable(kind: String) = "user_liked_$kind"

    @ParameterizedTest
    @ValueSource(strings = ["events", "places", "performers"])
    fun `likes need a signed-in user`(kind: String) {
        val id = target(kind)
        mockMvc.get("/api/me/likes/$kind/$id").andExpect { status { isUnauthorized() } }
        mockMvc.put("/api/me/likes/$kind/$id").andExpect { status { isUnauthorized() } }
        mockMvc.delete("/api/me/likes/$kind/$id").andExpect { status { isUnauthorized() } }
    }

    @ParameterizedTest
    @ValueSource(strings = ["events", "places", "performers"])
    fun `like, check and unlike`(kind: String) {
        val sub = UUID.randomUUID()
        val id = target(kind)
        val url = "/api/me/likes/$kind/$id"

        mockMvc.get(url) { with(tokenFor(sub)) }.andExpect {
            status { isOk() }
            jsonPath("$.liked") { value(false) }
        }
        mockMvc.put(url) { with(tokenFor(sub)) }.andExpect {
            status { isOk() }
            jsonPath("$.liked") { value(true) }
        }
        mockMvc.put(url) { with(tokenFor(sub)) }.andExpect { jsonPath("$.liked") { value(true) } }
        assertEquals(1, count(likeTable(kind)))
        mockMvc.get(url) { with(tokenFor(sub)) }.andExpect { jsonPath("$.liked") { value(true) } }
        mockMvc.get(url) { with(tokenFor(UUID.randomUUID())) }.andExpect { jsonPath("$.liked") { value(false) } }

        mockMvc.delete(url) { with(tokenFor(sub)) }.andExpect {
            status { isOk() }
            jsonPath("$.liked") { value(false) }
        }
        mockMvc.delete(url) { with(tokenFor(sub)) }.andExpect { jsonPath("$.liked") { value(false) } }
        assertEquals(0, count(likeTable(kind)))
    }

    @ParameterizedTest
    @ValueSource(strings = ["events", "places", "performers"])
    fun `liking something that does not exist is not found`(kind: String) {
        val url = "/api/me/likes/$kind/${UUID.randomUUID()}"
        mockMvc.get(url) { with(tokenFor(UUID.randomUUID())) }.andExpect { status { isNotFound() } }
        mockMvc.put(url) { with(tokenFor(UUID.randomUUID())) }.andExpect { status { isNotFound() } }
        mockMvc.delete(url) { with(tokenFor(UUID.randomUUID())) }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `a new user's first requests in parallel all succeed`() {
        val sub = UUID.randomUUID()
        val place = data.place()
        val requests = List(PARALLEL) { index ->
            Callable {
                val url = if (index % 2 == 0) "/api/places/liked-places" else "/api/me/likes/places/${place.id}"
                val request = if (index % 2 == 0) {
                    mockMvc.get(url) { with(tokenFor(sub)) }
                } else {
                    mockMvc.put(url) { with(tokenFor(sub)) }
                }
                request.andReturn().response.status
            }
        }

        val statuses = Executors.newFixedThreadPool(PARALLEL).use { pool -> pool.invokeAll(requests).map { it.get() } }

        assertEquals(List(PARALLEL) { 200 }, statuses)
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM user_entity WHERE sub = ?", Int::class.java, sub))
        assertEquals(1, count("user_liked_places"))
    }

    private companion object {
        const val PARALLEL = 16
    }
}
