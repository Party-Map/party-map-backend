package com.partymap.backend.domain.admin

import com.partymap.backend.config.Roles
import com.partymap.backend.domain.admin.dto.AdminUserDto
import com.partymap.backend.domain.admin.dto.AdminUserPageDto
import com.partymap.backend.domain.common.exception.InvalidRequestException
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * The platform admin's view of the realm's users. Roles live in Keycloak, so nothing here touches the database; a
 * granted role reaches the user's token at their next sign-in or token refresh.
 */
@Service
class AdminUserService(private val users: KeycloakUsers) {
    /** Keycloak has no batch role lookup, so a page costs one role call per user; the controller caps [size]. */
    fun list(query: String?, page: Int, size: Int): AdminUserPageDto {
        val search = query?.trim()?.takeIf { it.isNotEmpty() }
        val first = (page.toLong() * size).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val items = users.search(search, first, size).map { it.toDto(users.realmRoles(it.id)) }
        return AdminUserPageDto(items, users.count(search), page, size)
    }

    fun get(id: UUID): AdminUserDto = users.get(id).toDto(users.realmRoles(id))

    /** Idempotent: granting a role the user already holds changes nothing. */
    fun grant(id: UUID, role: String) {
        requireManagerRole(role)
        users.get(id)
        if (role !in users.realmRoles(id)) users.addRealmRole(id, role)
    }

    /** Idempotent: revoking a role the user does not hold changes nothing. */
    fun revoke(id: UUID, role: String) {
        requireManagerRole(role)
        users.get(id)
        if (role in users.realmRoles(id)) users.removeRealmRole(id, role)
    }

    private fun requireManagerRole(role: String) {
        if (role !in Roles.MANAGER_ROLES) {
            throw InvalidRequestException(
                "Only the manager roles can be granted or revoked: ${Roles.MANAGER_ROLES.sorted().joinToString()}.",
            )
        }
    }

    private fun KeycloakUser.toDto(realmRoles: Set<String>) = AdminUserDto(
        id = id,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        enabled = enabled,
        roles = realmRoles.filter { it in Roles.ADMIN_VISIBLE_ROLES }.sorted(),
    )
}
