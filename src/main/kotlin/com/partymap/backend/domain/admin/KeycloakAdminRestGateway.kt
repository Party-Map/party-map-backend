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
import java.util.concurrent.ConcurrentHashMap

/**
 * [KeycloakUsers] over Keycloak's Admin REST API, authenticated as the service account of the confidential client in
 * [KeycloakAdminProperties] ([KeycloakAccessToken]). The token is dropped when Keycloak rejects it; role ids are cached
 * for the life of the application.
 */
class KeycloakAdminRestGateway(
    private val client: RestClient,
    private val properties: KeycloakAdminProperties,
    clock: Clock,
) : KeycloakUsers {
    private val token = KeycloakAccessToken(client, properties, clock)
    private val roleIds = ConcurrentHashMap<String, String>()
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

    override fun realmRoles(id: UUID): Set<String> = call(onNotFound = { NotFoundException("User", id) }) {
        client.get().uri("$users/{id}/role-mappings/realm", properties.realm, id)
            .authorized()
            .retrieve()
            .body<List<RoleRepresentation>>()
            .orEmpty()
            .mapTo(mutableSetOf()) { it.name }
    }

    override fun addRealmRole(id: UUID, role: String) = changeRealmRole(HttpMethod.POST, id, role)

    override fun removeRealmRole(id: UUID, role: String) = changeRealmRole(HttpMethod.DELETE, id, role)

    private fun changeRealmRole(method: HttpMethod, id: UUID, role: String) {
        val mapping = listOf(RoleRepresentation(roleId(role), role))
        call(onNotFound = { NotFoundException("User", id) }) {
            client.method(method).uri("$users/{id}/role-mappings/realm", properties.realm, id)
                .authorized()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapping)
                .retrieve()
                .toBodilessEntity()
        }
    }

    private fun roleId(role: String): String = roleIds[role] ?: call(
        onNotFound = { UpstreamException("The realm role $role does not exist in Keycloak.") },
    ) {
        client.get().uri("/admin/realms/{realm}/roles/{role}", properties.realm, role)
            .authorized()
            .retrieve()
            .body<RoleRepresentation>() ?: throw unexpectedKeycloakAnswer()
    }.id.also { roleIds[role] = it }

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
