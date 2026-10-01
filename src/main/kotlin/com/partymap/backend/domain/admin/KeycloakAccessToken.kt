package com.partymap.backend.domain.admin

import com.partymap.backend.domain.common.exception.UpstreamException
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body
import java.time.Clock
import java.time.Instant

/**
 * The service account's access token (OAuth 2 client credentials), cached until shortly before it expires. Thread-safe:
 * concurrent admin calls share one token request.
 */
internal class KeycloakAccessToken(
    private val client: RestClient,
    private val properties: KeycloakAdminProperties,
    private val clock: Clock,
) {
    private var cached: Cached? = null

    @Synchronized
    fun value(): String {
        val now = clock.instant()
        cached?.takeIf { now.isBefore(it.refreshAt) }?.let { return it.value }
        if (properties.clientSecret.isBlank()) throw UpstreamException("The Keycloak admin client is not configured.")

        val response = request()
        return Cached(response.accessToken, now.plusSeconds(response.expiresIn - REFRESH_MARGIN_SECONDS))
            .also { cached = it }
            .value
    }

    /** Drops the cached token, e.g. after Keycloak rejected it; the next call fetches a new one. */
    @Synchronized
    fun forget() {
        cached = null
    }

    private fun request(): TokenResponse {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
            add("client_id", properties.clientId)
            add("client_secret", properties.clientSecret)
        }
        return try {
            client.post().uri("/realms/{realm}/protocol/openid-connect/token", properties.realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body<TokenResponse>()
        } catch (ex: RestClientException) {
            throw failure(ex)
        } ?: throw unexpectedKeycloakAnswer()
    }

    private fun failure(ex: RestClientException): UpstreamException {
        if (ex !is HttpClientErrorException) return keycloakFailure(ex)
        log.warn("Keycloak refused the admin client's credentials: {}", ex.statusCode)
        return UpstreamException("Keycloak refused the admin client's credentials.")
    }

    private data class Cached(val value: String, val refreshAt: Instant)

    private companion object {
        /** Renew this long before the token expires, so a request never leaves with a token about to lapse. */
        const val REFRESH_MARGIN_SECONDS = 30L
        val log = LoggerFactory.getLogger(KeycloakAccessToken::class.java)!!
    }
}

private const val UNEXPECTED = "Keycloak did not answer as expected."
private val failureLog = LoggerFactory.getLogger("com.partymap.backend.domain.admin.Keycloak")!!

/** Keycloak could not be reached or answered with an error; only the exception type is logged, never its answer. */
internal fun keycloakFailure(ex: RestClientException): UpstreamException {
    failureLog.warn("Keycloak admin call failed: {}", ex.javaClass.simpleName)
    return UpstreamException(UNEXPECTED, ex)
}

/** Keycloak answered successfully but without the data the call needs. */
internal fun unexpectedKeycloakAnswer() = UpstreamException(UNEXPECTED)
