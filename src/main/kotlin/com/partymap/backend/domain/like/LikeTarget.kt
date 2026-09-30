package com.partymap.backend.domain.like

/** What a user can like, with the join table that stores the likes. */
enum class LikeTarget(val label: String, internal val table: String, internal val column: String) {
    EVENT("Event", "user_liked_events", "event_id"),
    PLACE("Place", "user_liked_places", "place_id"),
    PERFORMER("Performer", "user_liked_performers", "performer_id"),
}
