package com.partymap.backend.domain.place

import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationRepository
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationState
import com.partymap.backend.domain.eventplan.dto.EventPlanPlaceInvitationWithDateDto
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api")
class PlaceController(
    private val placeRepository: PlaceRepository,
    private val currentUserService: CurrentUserService,
    private val userLikesFetchService: UserLikesFetchService,
    private val eventPlanPlaceInvitationRepository: EventPlanPlaceInvitationRepository,
) {
    @GetMapping("/places")
    fun getPlaces(): List<PlaceDto> = placeRepository.findAll()
        .map { it.toDto() }

    @GetMapping("/places/{id}")
    fun getPlace(@PathVariable id: UUID): PlaceDto = placeRepository.findById(id)
        .orElseThrow { NoSuchElementException("Place $id not found") }
        .toDto()

    @GetMapping("/places/liked-places")
    fun getLikedPlacesForUser(@AuthenticationPrincipal jwt: Jwt): List<PlaceDto> {
        val user = currentUserService.getOrCreateUser(jwt)
        return userLikesFetchService.getLikedPlaces(user.sub)
    }

    @PreAuthorize("hasRole('place_manager_user')")
    @GetMapping("/places/owned-places")
    fun getMyPlacesForUser(@AuthenticationPrincipal jwt: Jwt): List<PlaceAdminListItemDto> {
        val user = currentUserService.getOrCreateUser(jwt)
        return placeRepository.findAllByOwnerSub(user.sub).map { it.toAdminListItemDto() }
    }

    @PreAuthorize("hasRole('place_manager_user')")
    @PostMapping("/places")
    fun createPlace(@AuthenticationPrincipal jwt: Jwt, @RequestBody dto: PlaceCreateDto): PlaceDto {
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
    @GetMapping("/places/{id}/invitations")
    fun getPlaceInvitations(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
    ): List<EventPlanPlaceInvitationWithDateDto> {
        currentUserService.getOrCreateUser(jwt)

        val place = placeRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Place $id not found") }

        val invitations = eventPlanPlaceInvitationRepository.findAllByPlaceId(place.id!!)

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
                "You are not allowed to manage invitations for this place",
            )
        }

        val invitation = eventPlanPlaceInvitationRepository
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

        eventPlanPlaceInvitationRepository.save(invitation)

        return HttpStatus.OK
    }
}
