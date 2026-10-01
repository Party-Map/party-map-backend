package com.partymap.backend.config

/** Keycloak realm roles, as they appear in the access token's `roles` claim (no `ROLE_` prefix). */
object Roles {
    const val USER = "user"
    const val EVENT_ORGANIZER = "event_organizer_user"
    const val PLACE_MANAGER = "place_manager_user"
    const val PERFORMER_MANAGER = "performer_manager_user"

    /** Platform administrator: manages who holds the manager roles (the `/api/admin` endpoints). */
    const val PARTYMAP_ADMIN = "partymap_admin"

    /** The roles a platform admin may grant and revoke; never [PARTYMAP_ADMIN] itself, that stays in Keycloak. */
    val MANAGER_ROLES: Set<String> = setOf(EVENT_ORGANIZER, PLACE_MANAGER, PERFORMER_MANAGER)

    /** The roles the admin user list shows; the rest (`user`, `offline_access`...) are Keycloak plumbing. */
    val ADMIN_VISIBLE_ROLES: Set<String> = MANAGER_ROLES + PARTYMAP_ADMIN
}
