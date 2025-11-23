package com.partymap.backend.domain.like.service

import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.performer.db.PerformerRepository
import org.springframework.stereotype.Service

@Service
class PerformerLikeService(
    performerRepository: PerformerRepository,
) : AbstractLikeService<PerformerEntity>(
    entityRepository = performerRepository,
    getLikedCollection = { user -> user.likedPerformers },
)
