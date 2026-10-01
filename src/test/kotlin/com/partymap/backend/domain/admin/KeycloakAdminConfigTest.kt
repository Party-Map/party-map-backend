package com.partymap.backend.domain.admin

import com.partymap.backend.domain.common.exception.UpstreamException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock

class KeycloakAdminConfigTest {
    private fun properties(secret: String) = KeycloakAdminProperties(
        baseUrl = "http://localhost:1",
        realm = "party-map",
        clientId = "c",
        clientSecret = secret,
    )

    @Test
    fun `builds the REST gateway`() {
        val users = KeycloakAdminConfig().keycloakUsers(properties("s"), Clock.systemUTC())

        assertInstanceOf(KeycloakAdminRestGateway::class.java, users)
    }

    @Test
    fun `starts without a secret and answers admin calls with a clear upstream error`() {
        val users = KeycloakAdminConfig().keycloakUsers(properties(""), Clock.systemUTC())

        val error = assertThrows<UpstreamException> { users.count(null) }
        assertEquals("The Keycloak admin client is not configured.", error.message)
    }

    @Test
    fun `an unreachable Keycloak is an upstream error`() {
        val users = KeycloakAdminConfig().keycloakUsers(properties("s"), Clock.systemUTC())

        assertThrows<UpstreamException> { users.count(null) }
    }
}
