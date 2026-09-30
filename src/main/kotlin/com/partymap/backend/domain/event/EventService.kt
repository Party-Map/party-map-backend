package com.partymap.backend.domain.event

import com.partymap.backend.domain.common.exception.NotFoundException
import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.event.dto.EventAdminListItemDto
import com.partymap.backend.domain.event.dto.EventDto
import com.partymap.backend.domain.event.dto.PlaceUpcomingEventDto
import com.partymap.backend.domain.like.dto.LikedEventsGroupedDto
import com.partymap.backend.domain.place.dto.PlaceDto
import com.partymap.backend.domain.place.toDto
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional(readOnly = true)
class EventService(private val eventRepository: EventRepository, private val clock: Clock) {
    /** Events at [placeId], or with [performerId] in the lineup, or all; in start order. */
    fun list(placeId: UUID?, performerId: UUID?): List<EventDto> {
        val events = when {
            placeId != null -> eventRepository.findAllByPlaceIdOrderByStartAsc(placeId)
            performerId != null -> eventRepository.findAllByPerformerId(performerId)
            else -> eventRepository.findAll(Sort.by("start"))
        }
        return events.map { it.toDto() }
    }

    fun get(id: UUID): EventDto = find(id).toDto()

    fun placeOf(id: UUID): PlaceDto = find(id).place.toDto()

    /** For each place, the event that is running or starts next. */
    fun upcomingPerPlace(): List<PlaceUpcomingEventDto> =
        eventRepository.findAllByEndAfterOrderByPlaceIdAscStartAsc(LocalDateTime.now(clock))
            .distinctBy { it.place.id }
            .map { it.toPlaceUpcomingEventDto() }

    /** The caller's liked events: not yet ended (soonest first) and ended (latest first). */
    fun likedGrouped(sub: UUID): LikedEventsGroupedDto {
        val now = LocalDateTime.now(clock)
        val (upcoming, past) = eventRepository.findAllByLikedByUsersSub(sub).partition { it.end.isAfter(now) }
        return LikedEventsGroupedDto(
            upcoming = upcoming.sortedBy { it.start }.map { it.toDto() },
            past = past.sortedByDescending { it.start }.map { it.toDto() },
        )
    }

    fun owned(sub: UUID): List<EventAdminListItemDto> =
        eventRepository.findAllByOwnerSubOrderByStartAsc(sub).map { it.toAdminListItemDto() }

    private fun find(id: UUID): EventEntity = eventRepository.findById(
        id,
    ).orElseThrow { NotFoundException("Event", id) }
}
