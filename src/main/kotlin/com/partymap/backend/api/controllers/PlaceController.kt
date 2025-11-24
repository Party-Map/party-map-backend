package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.PlaceDto
import com.partymap.backend.api.dtos.PlaceUpcomingEventDto
import com.partymap.backend.api.mappers.toDto
import com.partymap.backend.api.mappers.toPlaceUpcomingEventDto
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.like.service.UserLikesFetchService
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.user.service.CurrentUserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api")
class PlaceController(
    private val placeRepository: PlaceRepository,
    private val eventRepository: EventRepository,
    private val currentUserService: CurrentUserService,
    private val userLikesFetchService: UserLikesFetchService,
) {
    @GetMapping("/places")
    fun getPlaces(): List<PlaceDto> =
        placeRepository.findAll()
            .map { it.toDto() }

    @GetMapping("/places/{id}")
    fun getPlace(@PathVariable id: UUID): PlaceDto =
        placeRepository.findById(id)
            .orElseThrow { NoSuchElementException("Place $id not found") }
            .toDto()

    @GetMapping("/places/{id}/upcoming-event")
    fun getUpcomingEventForPlace(@PathVariable id: UUID): PlaceUpcomingEventDto {
        placeRepository.findById(id)
            .orElseThrow { NoSuchElementException("Place $id not found") }

        val now = Instant.now()

        val upcomingEvents = eventRepository
            .findAllByPlace_IdAndEndAfterOrderByStartAsc(id, now)

        val upcoming = upcomingEvents.minByOrNull { it.start }
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No upcoming event found for place $id",
            )

        return upcoming.toPlaceUpcomingEventDto()
    }
    @GetMapping("/places/liked-places")
    fun getLikedPlacesForUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<PlaceDto> {
        val user = currentUserService.getOrCreateUser(jwt)
        return userLikesFetchService.getLikedPlaces(user.sub)
    }
}