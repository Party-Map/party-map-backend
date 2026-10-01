package com.partymap.backend.domain.admin

import java.util.UUID

/** A Keycloak user as the admin pages need it; personal data stays in Keycloak, the API stores none of it. */
data class KeycloakUser(
    val id: UUID,
    val username: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val enabled: Boolean,
)

/**
 * The users and realm-role mappings of the realm. Implemented over Keycloak's Admin REST API
 * ([KeycloakAdminRestGateway]); tests replace it with an in-memory fake.
 *
 * Failures are `ApiException`s: an unknown user is a `NotFoundException`, anything Keycloak does wrong an
 * `UpstreamException` (502).
 */
interface KeycloakUsers {
    /** Users whose username, email, first or last name contains [query] (all users when blank), [first]-based page. */
    fun search(query: String?, first: Int, max: Int): List<KeycloakUser>

    /** How many users [search] would find without paging. */
    fun count(query: String?): Long

    fun get(id: UUID): KeycloakUser

    /** The names of the realm roles mapped directly to the user. */
    fun realmRoles(id: UUID): Set<String>

    fun addRealmRole(id: UUID, role: String)

    fun removeRealmRole(id: UUID, role: String)
}
