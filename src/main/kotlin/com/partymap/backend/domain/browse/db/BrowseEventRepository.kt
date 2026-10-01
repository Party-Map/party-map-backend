package com.partymap.backend.domain.browse.db

import com.partymap.backend.domain.event.db.EventEntity

/** Repository fragment behind `GET /api/browse/events`; [BrowseEventRepositoryImpl] builds the criteria query. */
interface BrowseEventRepository {
    fun browse(filter: EventBrowseFilter): Ranked<EventEntity>
}
