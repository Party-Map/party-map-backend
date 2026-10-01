package com.partymap.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.core.GrantedAuthorityDefaults
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

/**
 * Stateless bearer-token security. Reads are public except the caller's own data and the platform admin's paths; every
 * write needs a token, and the role and ownership rules sit on the controller methods (`@PreAuthorize`) and in the
 * services.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity, jwtAuthenticationConverter: JwtAuthenticationConverter): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/api/me/**", authenticated)
                authorize("/api/*/liked-*", authenticated)
                // Before the public GET rule: the admin reads (user lists) are not public.
                authorize("/api/admin/**", hasAuthority(Roles.PARTYMAP_ADMIN))
                authorize(HttpMethod.GET, "/**", permitAll)
                authorize("/error", permitAll)
                authorize(anyRequest, authenticated)
            }
            cors {}
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            oauth2ResourceServer { jwt { this.jwtAuthenticationConverter = jwtAuthenticationConverter } }
        }
        return http.build()
    }

    /** Keycloak realm roles arrive in the top-level `roles` claim and are used as authorities without a prefix. */
    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val authoritiesConverter = JwtGrantedAuthoritiesConverter()
        authoritiesConverter.setAuthoritiesClaimName("roles")
        authoritiesConverter.setAuthorityPrefix("")

        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter)
        return converter
    }

    companion object {
        /** `hasRole('place_manager_user')` matches the authority as is; static so method security sees it early. */
        @Bean
        @JvmStatic
        fun grantedAuthorityDefaults(): GrantedAuthorityDefaults = GrantedAuthorityDefaults("")
    }
}
