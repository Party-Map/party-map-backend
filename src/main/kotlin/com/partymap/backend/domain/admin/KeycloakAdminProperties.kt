package com.partymap.backend.domain.admin

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * The confidential Keycloak client whose service account manages users (`app.keycloak.admin.*`). Its roles in Keycloak:
 * `realm-management` → `view-users`, `query-users`, `manage-users`.
 */
@ConfigurationProperties("app.keycloak.admin")
data class KeycloakAdminProperties(
    /** Keycloak's root URL without the realm, e.g. `https://auth.terkep.party`. */
    val baseUrl: String,
    val realm: String,
    val clientId: String,
    /** Empty where no service account exists; the admin endpoints then answer 502 and the rest of the API works. */
    val clientSecret: String = "",
    val connectTimeout: Duration = DEFAULT_CONNECT_TIMEOUT,
    val readTimeout: Duration = DEFAULT_READ_TIMEOUT,
)

private val DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(3)
private val DEFAULT_READ_TIMEOUT = Duration.ofSeconds(5)
