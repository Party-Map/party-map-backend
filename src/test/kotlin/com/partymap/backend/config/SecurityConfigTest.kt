package com.partymap.backend.config

import com.partymap.backend.support.IntegrationTest
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class SecurityConfigTest : IntegrationTest() {
    @Autowired
    private lateinit var converter: JwtAuthenticationConverter

    @Test
    fun `realm roles from the roles claim become authorities without a prefix`() {
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("0b7c7a52-4c1f-4f7e-9e84-1f0f3c0c9a11")
            .claim("roles", listOf(Roles.USER, Roles.PLACE_MANAGER))
            .build()

        val authentication = converter.convert(jwt)!!

        val authorities = authentication.authorities.map { it.authority }
        assertTrue(authorities.containsAll(listOf(Roles.USER, Roles.PLACE_MANAGER)), authorities.toString())
        assertTrue(authorities.none { it!!.startsWith("ROLE_") }, authorities.toString())
    }

    @Test
    fun `public reads need no token and create no session`() {
        val response = mockMvc.get("/api/places").andExpect { status { isOk() } }.andReturn().response

        assertNull(response.getHeader("Set-Cookie"))
    }

    @Test
    fun `an invalid bearer token is refused`() {
        mockMvc.get("/api/places/liked-places") { header("Authorization", "Bearer not-a-jwt") }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `writes without a token are unauthorized, not forbidden`() {
        mockMvc.post("/api/places") { json(mapOf("name" to "x")) }.andExpect {
            status { isUnauthorized() }
            header { string("WWW-Authenticate", startsWith("Bearer")) }
        }
    }

    @Test
    fun `unknown paths are a problem-detail 404`() {
        mockMvc.get("/api/nothing-here").andExpect {
            status { isNotFound() }
            jsonPath("$.status") { value(404) }
        }
    }
}
