package com.partymap.backend.domain.admin

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Clock

/**
 * Wires [KeycloakUsers] to Keycloak. A missing client secret does not stop the application: the public API keeps
 * working and only the admin endpoints answer 502 with a message saying what is missing.
 */
@Configuration
@EnableConfigurationProperties(KeycloakAdminProperties::class)
class KeycloakAdminConfig {
    @Bean
    fun keycloakUsers(properties: KeycloakAdminProperties, clock: Clock): KeycloakUsers {
        if (properties.clientSecret.isBlank()) {
            log.warn("app.keycloak.admin.client-secret is empty: the /api/admin endpoints will answer 502")
        }
        val http = HttpClient.newBuilder().connectTimeout(properties.connectTimeout).build()
        val requestFactory = JdkClientHttpRequestFactory(http).apply { setReadTimeout(properties.readTimeout) }
        val client = RestClient.builder().baseUrl(properties.baseUrl).requestFactory(requestFactory).build()
        return KeycloakAdminRestGateway(client, properties, clock)
    }

    private companion object {
        val log = LoggerFactory.getLogger(KeycloakAdminConfig::class.java)!!
    }
}
