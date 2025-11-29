package com.partymap.backend.domain.place

import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.event.dto.PlaceUpcomingEventDto
import com.partymap.backend.domain.event.toPlaceUpcomingEventDto
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationEntityRepository
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationState
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationWithDateDto
import com.partymap.backend.domain.like.service.UserLikesFetchService
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.place.dto.PlaceAdminListItemDto
import com.partymap.backend.domain.place.dto.PlaceCreateDto
import com.partymap.backend.domain.place.dto.PlaceDto
import com.partymap.backend.domain.user.CurrentUserService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api")
class PlaceController(
    private val placeRepository: PlaceRepository,
    private val eventRepository: EventRepository,
    private val currentUserService: CurrentUserService,
    private val userLikesFetchService: UserLikesFetchService,
    private val eventPlanPlaceInvitationEntityRepository: EventPlanPlaceInvitationEntityRepository,
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

        val now = LocalDateTime.now()

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

    @PreAuthorize("hasRole('place_manager_user')")
    @GetMapping("/places/owned-places")
    fun getMyPlacesForUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<PlaceAdminListItemDto> {
        val user = currentUserService.getOrCreateUser(jwt)
        return placeRepository.findAllByOwner_Sub(user.sub).map { it.toAdminListItemDto() }
    }

    @PreAuthorize("hasRole('place_manager_user')")
    @PostMapping("/places")
    fun createPlace(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody dto: PlaceCreateDto,
    ): PlaceDto {
        val user = currentUserService.getOrCreateUser(jwt)
        val entity = dto.toEntity(user)
        val saved = placeRepository.save(entity)
        return saved.toDto()
    }

    @PreAuthorize("hasRole('place_manager_user')")
    @PutMapping("/places/{id}")
    fun updatePlace(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @RequestBody dto: PlaceCreateDto,
    ): PlaceDto {
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

    @PreAuthorize("hasRole('place_manager_user')")
    @DeleteMapping("/places/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePlace(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
    ) {
        val user = currentUserService.getOrCreateUser(jwt)

        val place = placeRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Place $id not found") }

        if (place.owner.sub != user.sub) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this place")
        }

        placeRepository.deleteById(id)
    }

    @PreAuthorize("hasRole('place_manager_user')")
    @GetMapping("/places/{id}/invitations")
    fun getPlaceInvitations(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
    ): List<EventPlanPlaceInvitationWithDateDto> {
        currentUserService.getOrCreateUser(jwt)

        val place = placeRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Place $id not found") }

        val invitations = eventPlanPlaceInvitationEntityRepository.findAllByPlaceId(place.id!!)

        return invitations.map {
            EventPlanPlaceInvitationWithDateDto(
                eventPlanId = it.id.eventPlan.id!!,
                state = it.state,
                title = it.id.eventPlan.title,
                startDateTime = it.id.eventPlan.startDateTime,
                endDateTime = it.id.eventPlan.endDateTime,
            )
        }
    }

    @PreAuthorize("hasRole('place_manager_user')")
    @PutMapping("/places/{id}/invitations/{eventPlanId}/respond")
    @Transactional
    fun respondToPlaceInvitation(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @PathVariable eventPlanId: UUID,
        @RequestParam state: String,
    ): HttpStatus {
        val user = currentUserService.getOrCreateUser(jwt)

        val place = placeRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Place $id not found") }

        if (place.owner.sub != user.sub) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to manage invitations for this place"
            )
        }

        val invitation = eventPlanPlaceInvitationEntityRepository
            .findByPlaceIdAndEventPlanId(place.id!!, eventPlanId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Invitation for event plan $eventPlanId not found for place $id",
                )
            }

        when (state.lowercase()) {
            "accept" -> invitation.state = EventPlanPlaceInvitationState.ACCEPTED
            "reject" -> invitation.state = EventPlanPlaceInvitationState.REJECTED
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state: $state")
        }

        eventPlanPlaceInvitationEntityRepository.save(invitation)

        return HttpStatus.OK
    }
}