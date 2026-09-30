package com.partymap.backend.domain.like.service

import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventRepository
import org.springframework.stereotype.Service

@Service
class EventLikeService(eventRepository: EventRepository) :
    AbstractLikeService<EventEntity>(
        entityRepository = eventRepository,
        getLikedCollection = { user -> user.likedEvents },
    )
