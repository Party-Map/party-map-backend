package com.partymap.backend.support

import com.partymap.backend.domain.admin.KeycloakUser
import com.partymap.backend.domain.admin.KeycloakUsers
import com.partymap.backend.domain.common.exception.NotFoundException
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Primary
import java.util.UUID

/**
 * In-memory Keycloak for integration tests: replaces the REST gateway in the shared test context (the gateway itself is
 * covered by `KeycloakAdminRestGatewayTest`). Matches searches like Keycloak does: a case-insensitive substring of the
 * username, email, first or last name. Reset after every test by `IntegrationTest`.
 */
@TestComponent
@Primary
class FakeKeycloakUsers : KeycloakUsers {
    private val users = linkedMapOf<UUID, KeycloakUser>()
    private val roles = mutableMapOf<UUID, MutableSet<String>>()

    /** Thrown by every call while set, to simulate Keycloak being down or misconfigured. */
    var failure: RuntimeException? = null

    private var searched: Triple<String?, Int, Int>? = null

    /** The arguments of the last [search] call. */
    val lastSearch: Triple<String?, Int, Int>? get() = searched

    fun reset() {
        users.clear()
        roles.clear()
        failure = null
        searched = null
    }

    fun user(
        username: String,
        vararg realmRoles: String,
        firstName: String? = null,
        lastName: String? = null,
        enabled: Boolean = true,
    ): KeycloakUser {
        val user = KeycloakUser(UUID.randomUUID(), username, username, firstName, lastName, enabled)
        users[user.id] = user
        roles[user.id] = realmRoles.toMutableSet()
        return user
    }

    fun rolesOf(id: UUID): Set<String> = roles.getValue(id).toSet()

    override fun search(query: String?, first: Int, max: Int): List<KeycloakUser> {
        failure?.let { throw it }
        searched = Triple(query, first, max)
        return matching(query).drop(first).take(max)
    }

    override fun count(query: String?): Long {
        failure?.let { throw it }
        return matching(query).size.toLong()
    }

    override fun get(id: UUID): KeycloakUser {
        failure?.let { throw it }
        return users[id] ?: throw NotFoundException("User", id)
    }

    override fun realmRoles(id: UUID): Set<String> = rolesOf(get(id).id)

    override fun addRealmRole(id: UUID, role: String) {
        roles.getValue(get(id).id).add(role)
    }

    override fun removeRealmRole(id: UUID, role: String) {
        roles.getValue(get(id).id).remove(role)
    }

    private fun matching(query: String?): List<KeycloakUser> = users.values.filter { user ->
        query.isNullOrBlank() ||
            listOfNotNull(user.username, user.email, user.firstName, user.lastName)
                .any { it.contains(query, ignoreCase = true) }
    }
}
