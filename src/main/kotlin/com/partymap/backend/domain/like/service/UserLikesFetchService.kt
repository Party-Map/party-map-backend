package com.partymap.backend.domain.like.service

import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.event.toDto
import com.partymap.backend.domain.like.dto.LikedEventsGroupedDto
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.performer.dto.PerformerDto
import com.partymap.backend.domain.performer.toDto
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.place.dto.PlaceDto
import com.partymap.backend.domain.place.toDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class UserLikesFetchService(
    private val eventRepository: EventRepository,
    private val placeRepository: PlaceRepository,
    private val performerRepository: PerformerRepository,
) {
    fun getLikedEventsGrouped(sub: UUID): LikedEventsGroupedDto {
        val events = eventRepository.findAllByLikedByUsers_Sub(sub)

        val now = LocalDateTime.now()

        val upcoming = events
            .filter { it.end.isAfter(now) }
            .sortedBy { it.start }
            .map { it.toDto() }

        val past = events
            .filter { it.end.isBefore(now) }
            .sortedByDescending { it.start }
            .map { it.toDto() }

        return LikedEventsGroupedDto(
            upcoming = upcoming,
            past = past,
        )
    }

    fun getLikedPlaces(sub: UUID): List<PlaceDto> = placeRepository.findAllByLikedByUsers_Sub(sub)
        .map { it.toDto() }

    fun getLikedPerformers(sub: UUID): List<PerformerDto> = performerRepository.findAllByLikedByUsers_Sub(sub)
        .map { it.toDto() }
}
