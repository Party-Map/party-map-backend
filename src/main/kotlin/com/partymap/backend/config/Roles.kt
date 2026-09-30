package com.partymap.backend.config

/** Keycloak realm roles, as they appear in the access token's `roles` claim (no `ROLE_` prefix). */
object Roles {
    const val USER = "user"
    const val EVENT_ORGANIZER = "event_organizer_user"
    const val PLACE_MANAGER = "place_manager_user"
    const val PERFORMER_MANAGER = "performer_manager_user"
}
