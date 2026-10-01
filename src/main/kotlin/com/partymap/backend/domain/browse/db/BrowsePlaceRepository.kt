package com.partymap.backend.domain.browse.db

import com.partymap.backend.domain.place.db.PlaceEntity

/** Repository fragment behind `GET /api/browse/places`; [BrowsePlaceRepositoryImpl] builds the criteria query. */
interface BrowsePlaceRepository {
    fun browse(filter: PlaceBrowseFilter): Ranked<PlaceEntity>
}
