package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.PlaceAdminListItemDto
import com.partymap.backend.api.dtos.PlaceCreateDto
import com.partymap.backend.api.dtos.PlaceDto
import com.partymap.backend.api.dtos.PlaceUpcomingEventDto
import com.partymap.backend.api.mappers.toAdminListItemDto
import com.partymap.backend.api.mappers.toDto
import com.partymap.backend.api.mappers.toEntity
import com.partymap.backend.api.mappers.toPlaceUpcomingEventDto
import com.partymap.backend.api.mappers.updateFromDto
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.like.service.UserLikesFetchService
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.security.getRealmRoles
import com.partymap.backend.domain.user.service.CurrentUserService
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @GetMapping("/places/owned-places")
    fun getMyPlacesForUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<PlaceAdminListItemDto> {
        val user = currentUserService.getOrCreateUser(jwt)
        if ("place_manager_user" !in jwt.getRealmRoles()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed view places as non-manager")
        }
        return placeRepository.findAllByOwner_Sub(user.sub).map { it.toAdminListItemDto() }
    }

    @PostMapping("/places")
    fun createPlace(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody dto: PlaceCreateDto,
    ): PlaceDto {
        if ("place_manager_user" !in jwt.getRealmRoles()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to create a place")
        }
        val user = currentUserService.getOrCreateUser(jwt)
        val entity = dto.toEntity(user)
        val saved = placeRepository.save(entity)
        return saved.toDto()
    }

    @PutMapping("/places/{id}")
    fun updatePlace(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @RequestBody dto: PlaceCreateDto,
    ): PlaceDto {
        if ("place_manager_user" !in jwt.getRealmRoles()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update a place")
        }
        val user = currentUserService.getOrCreateUser(jwt)

        val place = placeRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Place $id not found") }

        if (place.owner.sub != user.sub) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this place")
        }

        place.updateFromDto(dto)

        val saved = placeRepository.save(place)
        return saved.toDto()
    }

}