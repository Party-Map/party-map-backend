package com.partymap.backend.domain.eventplan

import com.partymap.backend.domain.eventplan.db.*
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.place.dto.PlaceAdminListItemDto
import com.partymap.backend.domain.place.toAdminListItemDto
import com.partymap.backend.domain.user.CurrentUserService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api")
class EventPlanController(
    private val placeRepository: PlaceRepository,
    private val currentUserService: CurrentUserService,
    private val eventPlanRepository: EventPlanRepository,
    private val eventPlanService: EventPlanService
) {

    @PreAuthorize("hasRole('event_organizer_user')")
    @GetMapping("/event-plan/{id}")
    fun getEventPlan(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
    ): EventPlanDto {
        val user = currentUserService.getOrCreateUser(jwt)

        val eventPlan = eventPlanRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Event plan $id not found") }

        if (eventPlan.owner.sub != user.sub) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this event plan")
        }

        return eventPlan.toDto()
    }

    @PreAuthorize("hasRole('event_organizer_user')")
    @GetMapping("/event-plan/places")
    fun getPlacesForEventPlanList(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<PlaceAdminListItemDto> {
        currentUserService.getOrCreateUser(jwt)
        return placeRepository.findAll().map { it.toAdminListItemDto() }
    }

    @PreAuthorize("hasRole('event_organizer_user')")
    @GetMapping("/event-plan/owned-event-plans")
    fun getMyOwnedEventsForUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<EventPlanAdminListItemDto> {
        val user = currentUserService.getOrCreateUser(jwt)
        return eventPlanRepository.findAllByOwner_Sub(user.sub).map { it.toAdminListItemDto() }
    }

    @PreAuthorize("hasRole('event_organizer_user')")
    @PostMapping("/event-plan")
    fun createPlace(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody dto: EventPlanCreateDto,
    ): EventPlanDto {
        val user = currentUserService.getOrCreateUser(jwt)
        val entity = dto.toEntity(user)
        val saved = eventPlanRepository.save(entity)
        return saved.toDto()
    }

    @PreAuthorize("hasRole('event_organizer_user')")
    @PutMapping("/event-plan/{id}")
    fun updatePlace(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @RequestBody dto: EventPlanCreateDto,
    ): EventPlanDto {
        val user = currentUserService.getOrCreateUser(jwt)

        val eventPlan = eventPlanRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Event plan $id not found") }

        if (eventPlan.owner.sub != user.sub) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this event plan")
        }

        eventPlan.updateFromDto(dto)

        val saved = eventPlanRepository.save(eventPlan)
        return saved.toDto()
    }

    @PreAuthorize("hasRole('event_organizer_user')")
    @PutMapping("/event-plan/{id}/invite-place/{placeId}")
    fun invitePlaceForEventPlan(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @PathVariable placeId: UUID,
    ): HttpStatus {
        currentUserService.getOrCreateUser(jwt)

        val eventPlan = eventPlanRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Event plan $id not found")
        }

        val place = placeRepository.findById(placeId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Place $placeId not found") }

        eventPlanService.invitePlace(eventPlan, place)

        return HttpStatus.OK
    }

    @PreAuthorize("hasRole('place_manager_user')")
    @PostMapping("/event-plan/{id}/place-invitation/{placeId}/set-status")
    @Transactional
    fun setPlaceInvitationStatus(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @PathVariable placeId: UUID,
        @RequestParam status: String,
    ): HttpStatus {
        val user = currentUserService.getOrCreateUser(jwt)

        val eventPlan = eventPlanRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Event plan $id not found")
        }

        val place = placeRepository.findById(placeId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Place $placeId not found") }

        if (place.owner.sub != user.sub) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to manage invitations for this place"
            )
        }

        val placeInvitationOptional = Optional.ofNullable(eventPlan.placeInvitations.getOrNull(0))

        if (placeInvitationOptional.isPresent) {
            val placeInvitation = placeInvitationOptional.get()
            placeInvitation.state = when (status.lowercase()) {
                "accepted" -> EventPlanPlaceInvitationState.ACCEPTED
                "rejected" -> EventPlanPlaceInvitationState.REJECTED
                else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: $status")
            }
        } else {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No invitation found for place $placeId in event plan $id"
            )
        }

        eventPlanRepository.save(eventPlan)

        return HttpStatus.OK
    }

}