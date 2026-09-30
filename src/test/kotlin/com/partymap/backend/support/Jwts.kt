package com.partymap.backend.support

import com.partymap.backend.config.Roles
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.request.RequestPostProcessor
import java.util.UUID

/** A bearer token for [sub] holding [roles], shaped like the realm's access tokens (top-level `roles` claim). */
fun tokenFor(sub: UUID, vararg roles: String): RequestPostProcessor = jwt()
    .jwt { it.subject(sub.toString()).claim("roles", listOf(Roles.USER, *roles)) }
    .authorities(listOf(Roles.USER, *roles).map(::SimpleGrantedAuthority))
