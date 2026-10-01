package com.partymap.backend

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
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
class OpenApiExportTest @Autowired constructor(private val mockMvc: MockMvc, private val objectMapper: JsonMapper) {
    @Test
    fun `serves the OpenAPI document for every controller and exports it`() {
        val body = mockMvc.get("/api/openapi")
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsString

        val document = objectMapper.readTree(body)
        assertTrue(document.path("openapi").asText().startsWith("3."))
        val paths = document.path("paths").propertyNames().toSet()
        val expected = setOf(
            "/api/events", "/api/events/{id}", "/api/events/upcoming-events",
            "/api/places", "/api/places/{id}", "/api/places/{id}/invitations",
            "/api/performers", "/api/performers/{id}",
            "/api/event-plan", "/api/event-plan/{id}/publish",
            "/api/me/likes/events/{eventId}", "/api/search",
            "/api/admin/users", "/api/admin/users/{id}", "/api/admin/users/{id}/roles/{role}",
            "/api/browse/events", "/api/browse/places", "/api/browse/performers",
            "/api/browse/place-tags", "/api/browse/performer-genres",
        )
        assertEquals(emptySet<String>(), expected - paths, "missing paths")
        assertFalse(paths.any { it.endsWith("/set-status") }, "the removed set-status endpoint is still documented")
        // The HTML shells (/events/{id}, /sitemap.xml...) are pages, not API: springdoc only documents /api/**.
        assertTrue(
            paths.all { it.startsWith("/api/") },
            "non-API paths documented: ${paths.filterNot { it.startsWith("/api/") }}",
        )
        // Non-null Kotlin properties are required: the frontend's generated types depend on it.
        val placeRequired = document.path("components").path("schemas").path("PlaceDto").path("required")
            .values().map { it.asString() }.toSet()
        assertEquals(setOf("id", "name", "location", "address", "city", "tags", "links"), placeRequired)
        val adminUserRequired = document.path("components").path("schemas").path("AdminUserDto").path("required")
            .values().map { it.asString() }.toSet()
        assertEquals(setOf("id", "username", "enabled", "roles"), adminUserRequired)
        // Browse rows: the image, price and distance are optional, the venue summary is not.
        val browseEventRequired = document.path("components").path("schemas").path("BrowseEventItemDto")
            .path("required").values().map { it.asString() }.toSet()
        assertEquals(setOf("id", "title", "start", "end", "kind", "place"), browseEventRequired)
        // Browse filters are documented as individual query parameters, not as one object.
        val browseParameters = document.path("paths").path("/api/browse/events").path("get").path("parameters")
            .values().map { it.path("name").asString() }.toSet()
        assertTrue(
            browseParameters.containsAll(
                setOf("lat", "lon", "radiusKm", "from", "to", "kind", "q", "sort", "page", "size"),
            ),
        )
        // The role path variable is documented as one of the manager roles, so the frontend gets a union type.
        val roleParameter = document.path("paths").path("/api/admin/users/{id}/roles/{role}").path("put")
            .path("parameters").values().first { it.path("name").asString() == "role" }
        assertEquals(
            setOf("event_organizer_user", "place_manager_user", "performer_manager_user"),
            roleParameter.path("schema").path("enum").values().map { it.asString() }.toSet(),
        )

        val out = Path.of("build", "openapi.json")
        Files.createDirectories(out.parent)
        Files.writeString(
            out,
            objectMapper.writer().with(SerializationFeature.INDENT_OUTPUT).writeValueAsString(document) + "\n",
        )
    }
}
