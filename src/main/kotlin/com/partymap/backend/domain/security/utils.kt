package com.partymap.backend.domain.security

import org.springframework.security.oauth2.jwt.Jwt

fun Jwt.getRealmRoles(): List<String> {
    val realmAccess = getClaim<Map<String, Any>>("realm_access") ?: return emptyList()
    return (realmAccess["roles"] as? Collection<*>)?.map { it.toString() } ?: emptyList()
}