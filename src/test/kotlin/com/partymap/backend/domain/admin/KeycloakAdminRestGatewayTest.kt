package com.partymap.backend.domain.admin

import com.partymap.backend.domain.common.exception.NotFoundException
import com.partymap.backend.domain.common.exception.UpstreamException
import com.partymap.backend.support.MutableClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withException
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.io.IOException
import java.time.Duration
import java.util.UUID

class KeycloakAdminRestGatewayTest {
    private val base = "http://keycloak.test"
    private val tokenUrl = "$base/realms/party-map/protocol/openid-connect/token"
    private val usersUrl = "$base/admin/realms/party-map/users"
    private val userId = UUID.fromString("3241fc43-0124-48ae-8850-eb5ac64559c6")
    private val clock = MutableClock()
    private val builder = RestClient.builder().baseUrl(base)
    private val server = MockRestServiceServer.bindTo(builder).build()

    private fun gateway(secret: String = "s3cret") = KeycloakAdminRestGateway(
        builder.build(),
        KeycloakAdminProperties(
            baseUrl = base,
            realm = "party-map",
            clientId = "partymap-backend",
            clientSecret = secret,
        ),
        clock,
    )

    private fun expectToken(value: String = "t1", expiresIn: Int = 60) {
        server.expect(requestTo(tokenUrl))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(
                content().formDataContains(
                    mapOf(
                        "grant_type" to "client_credentials",
                        "client_id" to "partymap-backend",
                        "client_secret" to "s3cret",
                    ),
                ),
            )
            .andRespond(json("""{"access_token":"$value","expires_in":$expiresIn,"token_type":"Bearer"}"""))
    }

    private fun expectCount(bearer: String, answer: String) {
        server.expect(requestTo("$usersUrl/count"))
            .andExpect(header("Authorization", "Bearer $bearer"))
            .andRespond(json(answer))
    }

    private fun json(body: String) = withSuccess(body, MediaType.APPLICATION_JSON)

    private val userJson = """
        {"id":"$userId","username":"adrian@szell.dev","email":"adrian@szell.dev","firstName":"Adrián",
         "lastName":"Széll","enabled":true,"createdTimestamp":1}
    """.trimIndent()

    @Test
    fun `fetches one token for several calls and sends it as a bearer token`() {
        val gateway = gateway()
        expectToken()
        repeat(2) {
            server.expect(requestTo("$usersUrl/count"))
                .andExpect(header("Authorization", "Bearer t1"))
                .andRespond(json("7"))
        }

        assertEquals(7, gateway.count(null))
        assertEquals(7, gateway.count(""))
        server.verify()
    }

    @Test
    fun `fetches a new token shortly before the old one expires`() {
        val gateway = gateway()
        expectToken("t1", expiresIn = 60)
        expectCount(bearer = "t1", answer = "1")
        expectToken("t2", expiresIn = 60)
        expectCount(bearer = "t2", answer = "1")

        gateway.count(null)
        clock.advance(Duration.ofSeconds(31))
        gateway.count(null)
        server.verify()
    }

    @Test
    fun `without a client secret nothing is sent and the caller learns it is not configured`() {
        val gateway = gateway(secret = " ")

        val error = assertThrows<UpstreamException> { gateway.count(null) }

        assertEquals("The Keycloak admin client is not configured.", error.message)
        server.verify()
    }

    @Test
    fun `refused client credentials are reported without Keycloak's answer`() {
        val gateway = gateway()
        server.expect(requestTo(tokenUrl)).andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("""{"error":"x"}"""))

        val error = assertThrows<UpstreamException> { gateway.count(null) }

        assertEquals("Keycloak refused the admin client's credentials.", error.message)
    }

    @Test
    fun `an unreachable token endpoint is an upstream failure`() {
        val gateway = gateway()
        server.expect(requestTo(tokenUrl)).andRespond(withException(IOException("connection refused")))

        assertThrows<UpstreamException> { gateway.count(null) }
    }

    @Test
    fun `a token answer without a body is an upstream failure`() {
        val gateway = gateway()
        server.expect(requestTo(tokenUrl)).andRespond(withSuccess())

        assertThrows<UpstreamException> { gateway.count(null) }
    }

    @Test
    fun `searches with paging and leaves out a blank search`() {
        val gateway = gateway()
        expectToken()
        server.expect { request ->
            assertEquals("/admin/realms/party-map/users", request.uri.path)
            assertFalse(request.uri.query.contains("search"), request.uri.query)
        }.andExpect(queryParam("first", "40"))
            .andExpect(queryParam("max", "20"))
            .andExpect(queryParam("briefRepresentation", "true"))
            .andRespond(json("[$userJson]"))

        val users = gateway.search(" ", first = 40, max = 20)

        assertEquals(
            listOf(KeycloakUser(userId, "adrian@szell.dev", "adrian@szell.dev", "Adrián", "Széll", enabled = true)),
            users,
        )
        server.verify()
    }

    @Test
    fun `searches for the text anywhere in the fields, encoded, and counts with the same matcher`() {
        // Keycloak matches prefixes unless the term is wrapped in *...*; "..." would mean an exact match.
        val gateway = gateway()
        expectToken()
        server.expect { request ->
            assertTrue(request.uri.rawQuery.contains("search=%2Aa%26b%20c%2A"), request.uri.rawQuery)
        }.andRespond(json("[]"))
        server.expect(requestTo("$usersUrl/count?search=%2Aa%26b%20c%2A")).andRespond(json("0"))

        assertEquals(emptyList<KeycloakUser>(), gateway.search(" a&b c ", 0, 10))
        assertEquals(0, gateway.count("a&b c"))
        server.verify()
    }

    @Test
    fun `typed wildcards and quotes do not change the kind of search`() {
        val gateway = gateway()
        expectToken()
        server.expect(requestTo("$usersUrl/count?search=%2Aadr%2A")).andRespond(json("1"))
        server.expect(requestTo("$usersUrl/count")).andRespond(json("9"))

        assertEquals(1, gateway.count("\"*adr*\""))
        assertEquals(9, gateway.count("**"))
        server.verify()
    }

    @Test
    fun `reads one user and treats missing optional fields as absent`() {
        val gateway = gateway()
        expectToken()
        server.expect(requestTo("$usersUrl/$userId"))
            .andRespond(json("""{"id":"$userId","username":"x"}"""))

        assertEquals(KeycloakUser(userId, "x", null, null, null, enabled = false), gateway.get(userId))
    }

    @Test
    fun `an unknown user is not found`() {
        val gateway = gateway()
        expectToken()
        server.expect(requestTo("$usersUrl/$userId")).andRespond(withStatus(HttpStatus.NOT_FOUND))

        val error = assertThrows<NotFoundException> { gateway.get(userId) }

        assertEquals("User $userId was not found.", error.message)
    }

    @Test
    fun `lists the realm roles of a user by name`() {
        val gateway = gateway()
        expectToken()
        server.expect(requestTo("$usersUrl/$userId/role-mappings/realm"))
            .andRespond(json("""[{"id":"r1","name":"user"},{"id":"r2","name":"place_manager_user"}]"""))

        assertEquals(setOf("user", "place_manager_user"), gateway.realmRoles(userId))
    }

    @Test
    fun `adds and removes realm roles with the role's id, resolving the id once`() {
        val gateway = gateway()
        expectToken()
        server.expect(ExpectedCount.once(), requestTo("$base/admin/realms/party-map/roles/place_manager_user"))
            .andRespond(json("""{"id":"role-1","name":"place_manager_user","composite":true}"""))
        server.expect(requestTo("$usersUrl/$userId/role-mappings/realm"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().json("""[{"id":"role-1","name":"place_manager_user"}]"""))
            .andRespond(withStatus(HttpStatus.NO_CONTENT))
        server.expect(requestTo("$usersUrl/$userId/role-mappings/realm"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(content().json("""[{"id":"role-1","name":"place_manager_user"}]"""))
            .andRespond(withStatus(HttpStatus.NO_CONTENT))

        gateway.addRealmRole(userId, "place_manager_user")
        gateway.removeRealmRole(userId, "place_manager_user")
        server.verify()
    }

    @Test
    fun `a role missing from the realm is a configuration problem, not a missing user`() {
        val gateway = gateway()
        expectToken()
        server.expect(requestTo("$base/admin/realms/party-map/roles/place_manager_user"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        val error = assertThrows<UpstreamException> { gateway.addRealmRole(userId, "place_manager_user") }

        assertEquals("The realm role place_manager_user does not exist in Keycloak.", error.message)
    }

    @Test
    fun `a forbidden admin call names the service account and forgets the token`() {
        val gateway = gateway()
        expectToken("t1")
        server.expect(requestTo("$usersUrl/count")).andRespond(withStatus(HttpStatus.FORBIDDEN))
        expectToken("t2")
        expectCount(bearer = "t2", answer = "3")

        val error = assertThrows<UpstreamException> { gateway.count(null) }
        assertTrue(error.message.contains("service-account"), error.message)

        assertEquals(3, gateway.count(null))
        server.verify()
    }

    @Test
    fun `a rejected token is forgotten too`() {
        val gateway = gateway()
        expectToken("t1")
        server.expect(requestTo("$usersUrl/$userId")).andRespond(withStatus(HttpStatus.UNAUTHORIZED))
        expectToken("t2")
        server.expect(requestTo("$usersUrl/$userId")).andRespond(json(userJson))

        assertThrows<UpstreamException> { gateway.get(userId) }
        assertEquals("adrian@szell.dev", gateway.get(userId).username)
        server.verify()
    }

    @Test
    fun `Keycloak failures become a 502 without the upstream body`() {
        val gateway = gateway()
        expectToken()
        server.expect(requestTo("$usersUrl/count"))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("stack trace with secrets"))
        server.expect(requestTo("$usersUrl/count")).andRespond(withStatus(HttpStatus.BAD_REQUEST).body("bad"))
        server.expect(requestTo("$usersUrl/count")).andRespond(withException(IOException("timeout")))

        repeat(3) {
            val error = assertThrows<UpstreamException> { gateway.count(null) }
            assertEquals(HttpStatus.BAD_GATEWAY, error.status)
            assertEquals("Keycloak did not answer as expected.", error.message)
        }
        server.verify()
    }

    @Test
    fun `an empty answer where data was expected is an upstream failure`() {
        val gateway = gateway()
        expectToken()
        server.expect(requestTo("$usersUrl/$userId")).andRespond(withSuccess())
        server.expect(requestTo("$usersUrl/count")).andRespond(withSuccess())
        server.expect(requestTo("$base/admin/realms/party-map/roles/user")).andRespond(withSuccess())

        assertThrows<UpstreamException> { gateway.get(userId) }
        assertThrows<UpstreamException> { gateway.count(null) }
        assertThrows<UpstreamException> { gateway.addRealmRole(userId, "user") }
    }

    @Test
    fun `empty list answers are empty results`() {
        val gateway = gateway()
        expectToken()
        server.expect(requestTo("$usersUrl/$userId/role-mappings/realm")).andRespond(withSuccess())
        server.expect { request -> assertEquals("/admin/realms/party-map/users", request.uri.path) }
            .andRespond(withSuccess())

        assertEquals(emptySet<String>(), gateway.realmRoles(userId))
        assertEquals(emptyList<KeycloakUser>(), gateway.search(null, 0, 5))
    }
}
