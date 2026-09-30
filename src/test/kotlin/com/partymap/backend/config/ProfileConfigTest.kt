package com.partymap.backend.config

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.cors.CorsConfigurationSource

/** The beans that only exist in the dev or prod profile, which the test profile never loads. */
class ProfileConfigTest {
    private fun CorsConfigurationSource.allowedOrigins() =
        getCorsConfiguration(MockHttpServletRequest("GET", "/api/places"))!!.allowedOrigins

    @Test
    fun `dev allows the local frontend and prod the live site`() {
        assertEquals(listOf("http://localhost:3000"), CorsConfig().devCorsConfigurationSource().allowedOrigins())
        assertEquals(listOf("https://terkep.party"), CorsConfig().prodCorsConfigurationSource().allowedOrigins())
    }

    @Test
    fun `dev recreates the schema on every start`() {
        val flyway = mock<Flyway>()

        DevDatabaseConfig().cleanMigrateStrategy().migrate(flyway)

        inOrder(flyway) {
            verify(flyway).clean()
            verify(flyway).migrate()
        }
    }
}
