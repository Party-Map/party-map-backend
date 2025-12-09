package com.partymap.backend.domain.event

import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.event.dto.EventAdminListItemDto
import com.partymap.backend.domain.event.dto.EventDto
import com.partymap.backend.domain.event.dto.PlaceUpcomingEventDto
import com.partymap.backend.domain.like.dto.LikedEventsGroupedDto
import com.partymap.backend.domain.like.service.UserLikesFetchService
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.performer.dto.PerformerDto
import com.partymap.backend.domain.performer.toDto
import com.partymap.backend.domain.place.dto.PlaceDto
import com.partymap.backend.domain.place.toDto
import com.partymap.backend.domain.user.CurrentUserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api")
class EventController(
    private val eventRepository: EventRepository,
    private val currentUserService: CurrentUserService,
    private val userLikesFetchService: UserLikesFetchService,
    private val performerRepository: PerformerRepository,
) {

    // /events?placeId=<UUID>
    // /events?performerId=<UUID>
    @GetMapping("/events")
    fun getEvents(
        @RequestParam(required = false) placeId: UUID?,
        @RequestParam(required = false) performerId: UUID?,
    ): List<EventDto> {
        val events = when {
            placeId != null -> eventRepository.findAllByPlace_Id(placeId)
            performerId != null -> performerRepository.findById(performerId).map { performerEntity ->
                performerEntity.lineupItems.map { it.id.event }
            }.orElse(emptyList())

            else -> eventRepository.findAll()
        }
        return events.map { it.toDto() }
    }

    @GetMapping("/events/{id}")
    fun getEvent(@PathVariable id: UUID): EventDto =
        eventRepository.findById(id)
            .orElseThrow { NoSuchElementException("Event $id not found") }
            .toDto()

    @GetMapping("/events/{id}/place")
    fun getPlaceByEventId(@PathVariable id: UUID): PlaceDto {
        val event = eventRepository.findById(id)
            .orElseThrow { NoSuchElementException("Event $id not found") }

        return event.place.toDto()
    }

    @GetMapping("/events/{id}/performers")
    fun getPerformersByEventId(@PathVariable id: UUID): List<PerformerDto> {
        val event = eventRepository.findById(id)
            .orElseThrow { NoSuchElementException("Event $id not found") }

        return event.lineupItems.map { it.id.performer.toDto() }.distinct()
    }

    @GetMapping("/events/upcoming-events")
    fun getUpcomingEventsForAllPlaces(): List<PlaceUpcomingEventDto> {
        val now = LocalDateTime.now()

        val allUpcoming = eventRepository.findAllByEndAfterOrderByPlace_IdAscStartAsc(now)

        val earliestPerPlace: List<EventEntity> =
            allUpcoming
                .groupBy { it.place.id!! }
                .mapNotNull { (_, eventsForPlace) ->
                    eventsForPlace.minByOrNull { it.start }
                }

        return earliestPerPlace
            .map { it.toPlaceUpcomingEventDto() }
    }

    @GetMapping("/events/liked-events")
    fun getLikedEventsForUser(
        @AuthenticationPrincipal jwt: Jwt
    ): LikedEventsGroupedDto {
        val user = currentUserService.getOrCreateUser(jwt)
        return userLikesFetchService.getLikedEventsGrouped(user.sub)
    }

    @PreAuthorize("hasRole('event_organizer_user')")
    @GetMapping("/events/owned-events")
    fun getMyOwnedEventsForUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<EventAdminListItemDto> {
        val user = currentUserService.getOrCreateUser(jwt)
        return eventRepository.findAllByOwner_Sub(user.sub).map { it.toAdminListItemDto() }
    }

}