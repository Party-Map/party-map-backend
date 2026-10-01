package com.partymap.backend.domain.admin

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

// The parts of Keycloak's Admin REST API representations the gateway reads; everything else is ignored.

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class TokenResponse(
    @param:JsonProperty("access_token") val accessToken: String,
    @param:JsonProperty("expires_in") val expiresIn: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class UserRepresentation(
    val id: UUID,
    val username: String,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val enabled: Boolean = false,
) {
    fun toUser() = KeycloakUser(id, username, email, firstName, lastName, enabled)
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class RoleRepresentation(val id: String, val name: String)
