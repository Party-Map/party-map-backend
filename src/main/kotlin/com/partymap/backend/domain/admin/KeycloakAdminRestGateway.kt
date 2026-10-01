package com.partymap.backend.domain.admin

import com.partymap.backend.domain.common.exception.ApiException
import com.partymap.backend.domain.common.exception.NotFoundException
import com.partymap.backend.domain.common.exception.UpstreamException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body
import java.time.Clock
import java.util.UUID

/**
 * [KeycloakUsers] over Keycloak's Admin REST API, authenticated as the service account of the confidential client in
 * [KeycloakAdminProperties] ([KeycloakAccessToken]); the token is dropped when Keycloak rejects it. Needs only the
 * `realm-management` roles `view-users`, `query-users` and `manage-users`.
 */
class KeycloakAdminRestGateway(
    private val client: RestClient,
    private val properties: KeycloakAdminProperties,
    clock: Clock,
) : KeycloakUsers {
    private val token = KeycloakAccessToken(client, properties, clock)
    private val users = "/admin/realms/{realm}/users"

    override fun search(query: String?, first: Int, max: Int): List<KeycloakUser> = call {
        val search = infixSearch(query)
        client.get()
            .uri { uri ->
                uri.path(users)
                if (search != null) uri.queryParam("search", "{search}")
                uri.queryParam("first", first).queryParam("max", max).queryParam("briefRepresentation", true)
                    .build(mapOf("realm" to properties.realm, "search" to search.orEmpty()))
            }
            .authorized()
            .retrieve()
            .body<List<UserRepresentation>>()
            .orEmpty()
            .map { it.toUser() }
    }

    override fun count(query: String?): Long = call {
        val search = infixSearch(query)
        client.get()
            .uri { uri ->
                uri.path("$users/count")
                if (search != null) uri.queryParam("search", "{search}")
                uri.build(mapOf("realm" to properties.realm, "search" to search.orEmpty()))
            }
            .authorized()
            .retrieve()
            .body<Long>() ?: throw unexpectedKeycloakAnswer()
    }

    override fun get(id: UUID): KeycloakUser = call(onNotFound = { NotFoundException("User", id) }) {
        client.get().uri("$users/{id}", properties.realm, id)
            .authorized()
            .retrieve()
            .body<UserRepresentation>()
            ?.toUser() ?: throw unexpectedKeycloakAnswer()
    }

    override fun realmRoles(id: UUID): Set<String> = roleMappings(id).mapTo(mutableSetOf()) { it.name }

    /**
     * The role's id comes from the roles Keycloak lists as available to the user: reading `/roles/{name}` would need
     * `view-realm`, the user's role mappings only need the `manage-users` the service account has.
     */
    override fun addRealmRole(id: UUID, role: String) {
        val mapping = roleMappings(id, available = true).firstOrNull { it.name == role }
        if (mapping != null) {
            changeRoleMapping(HttpMethod.POST, id, mapping)
        } else if (role !in realmRoles(id)) {
            throw UpstreamException("The realm role $role does not exist in Keycloak.")
        }
    }

    override fun removeRealmRole(id: UUID, role: String) {
        roleMappings(id).firstOrNull { it.name == role }?.let { changeRoleMapping(HttpMethod.DELETE, id, it) }
    }

    /** The realm roles mapped directly to the user, or with [available] the ones that could still be mapped. */
    private fun roleMappings(id: UUID, available: Boolean = false): List<RoleRepresentation> {
        val path = if (available) "$users/{id}/role-mappings/realm/available" else "$users/{id}/role-mappings/realm"
        return call(onNotFound = { NotFoundException("User", id) }) {
            client.get().uri(path, properties.realm, id)
                .authorized()
                .retrieve()
                .body<List<RoleRepresentation>>()
                .orEmpty()
        }
    }

    private fun changeRoleMapping(method: HttpMethod, id: UUID, role: RoleRepresentation) {
        call(onNotFound = { NotFoundException("User", id) }) {
            client.method(method).uri("$users/{id}/role-mappings/realm", properties.realm, id)
                .authorized()
                .contentType(MediaType.APPLICATION_JSON)
                .body(listOf(role))
                .retrieve()
                .toBodilessEntity()
        }
    }

    /**
     * Keycloak's `search` matches prefixes, `*term*` anywhere and `"term"` exactly; the admin pages always search
     * anywhere, so typed wildcards and quotes are dropped. Null when nothing is left to search for.
     */
    private fun infixSearch(query: String?): String? =
        query?.filterNot { it == '*' || it == '"' }?.trim()?.takeIf { it.isNotEmpty() }?.let { "*$it*" }

    private fun <S : RestClient.RequestHeadersSpec<S>> S.authorized(): S =
        header("Authorization", "Bearer ${token.value()}")

    /** Runs one admin call and turns Keycloak's failures into [ApiException]s. */
    private fun <T> call(onNotFound: () -> ApiException = { unexpectedKeycloakAnswer() }, request: () -> T): T = try {
        request()
    } catch (ex: HttpClientErrorException) {
        throw when (HttpStatus.resolve(ex.statusCode.value())) {
            HttpStatus.NOT_FOUND -> onNotFound()

            HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN -> {
                token.forget()
                log.warn("Keycloak refused an admin call: {}", ex.statusCode)
                UpstreamException("The Keycloak admin client may not manage users. Check its service-account roles.")
            }

            else -> keycloakFailure(ex)
        }
    } catch (ex: RestClientException) {
        throw keycloakFailure(ex)
    }

    private companion object {
        val log = LoggerFactory.getLogger(KeycloakAdminRestGateway::class.java)!!
    }
}
