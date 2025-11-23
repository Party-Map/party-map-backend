package com.partymap.backend.domain.like.service

import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.place.db.PlaceRepository
import org.springframework.stereotype.Service

@Service
class PlaceLikeService(
    placeRepository: PlaceRepository,
) : AbstractLikeService<PlaceEntity>(
    entityRepository = placeRepository,
    getLikedCollection = { user -> user.likedPlaces },
)
