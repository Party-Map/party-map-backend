package com.partymap.backend.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

/**
 * Loads every profile's configuration the way Spring Boot does (duplicate keys are an error), because the tests run the
 * `test` profile only and a broken `application-dev.yml` would otherwise first fail when the dev stack starts.
 */
class ConfigFilesTest {
    private val files = PathMatchingResourcePatternResolver().getResources("classpath*:application*.yml")
        .filter { it.url.path.contains("/main/") }

    @Test
    fun `every profile's yaml loads`() {
        assertEquals(setOf("application.yml", "application-dev.yml"), files.map { it.filename }.toSet())
        for (file in files) {
            val sources = YamlPropertySourceLoader().load(file.filename!!, file)
            assertTrue(sources.isNotEmpty(), "${file.filename} is empty")
        }
    }

    @Test
    fun `the dev profile points the admin client at the dev Keycloak and keeps the issuer`() {
        val dev = YamlPropertySourceLoader().load("dev", files.first { it.filename == "application-dev.yml" }).single()

        assertEquals("\${APP_KEYCLOAK_ADMIN_URL:http://localhost:8081}", dev.getProperty("app.keycloak.admin.base-url"))
        assertEquals(
            "\${APP_SHELL_TEMPLATE_URL:http://127.0.0.1:3000/index.html}",
            dev.getProperty("app.shell.template-url"),
        )
        assertEquals(
            "https://auth.terkep.party/realms/party-map",
            dev.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri"),
        )
    }
}
