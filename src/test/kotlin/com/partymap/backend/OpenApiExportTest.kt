package com.partymap.backend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.nio.file.Files
import java.nio.file.Path

/**
 * Serves the OpenAPI document and writes it to build/openapi.json, the file the frontend
 * generates its API types from (see the frontend README for the refresh command).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfig::class)
class OpenApiExportTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) {
    @Test
    fun `serves the OpenAPI document for every controller and exports it`() {
        val body = mockMvc.get("/api/openapi")
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsString

        val document = objectMapper.readTree(body)
        assertTrue(document.path("openapi").asText().startsWith("3."))
        val paths = document.path("paths").fieldNames().asSequence().toSet()
        val expected = setOf(
            "/api/events", "/api/events/{id}", "/api/events/upcoming-events",
            "/api/places", "/api/places/{id}", "/api/places/{id}/invitations",
            "/api/performers", "/api/performers/{id}",
            "/api/event-plan", "/api/event-plan/{id}/publish",
            "/api/me/likes/events/{eventId}", "/api/search",
        )
        assertEquals(emptySet<String>(), expected - paths, "missing paths")

        val out = Path.of("build", "openapi.json")
        Files.createDirectories(out.parent)
        Files.writeString(out, objectMapper.writer().with(SerializationFeature.INDENT_OUTPUT).writeValueAsString(document) + "\n")
    }
}
