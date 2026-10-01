package com.partymap.backend.domain.admin.dto

import java.util.UUID

/** A Keycloak user as the platform admin sees it; [roles] holds only the manager roles and `partymap_admin`. */
data class AdminUserDto(
    val id: UUID,
    val username: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val enabled: Boolean,
    val roles: List<String>,
)

/** One page of users; [page] is zero-based and [total] counts every match of the search. */
data class AdminUserPageDto(val items: List<AdminUserDto>, val total: Long, val page: Int, val size: Int)
