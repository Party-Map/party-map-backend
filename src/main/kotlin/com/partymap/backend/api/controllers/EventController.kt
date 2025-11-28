package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.*
import com.partymap.backend.api.mappers.toAdminListItemDto
import com.partymap.backend.api.mappers.toDto
import com.partymap.backend.api.mappers.toPlaceUpcomingEventDto
import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.like.service.UserLikesFetchService
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.user.service.CurrentUserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api")
class EventController(
    private val eventRepository: EventRepository,
    private val currentUserService: CurrentUserService,
    private val userLikesFetchService: UserLikesFetchService,
    private val placeRepository: PlaceRepository,
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
            performerId != null -> eventRepository.findAllByPerformers_Id(performerId)
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

        return event.performers.map { it.toDto() }
    }

    @GetMapping("/events/upcoming-events")
    fun getUpcomingEventsForAllPlaces(): List<PlaceUpcomingEventDto> {
        val now = Instant.now()

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