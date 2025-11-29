package com.partymap.backend.domain.eventplan

import com.partymap.backend.domain.eventplan.db.EventPlanAdminListItemDto
import com.partymap.backend.domain.eventplan.db.EventPlanCreateDto
import com.partymap.backend.domain.eventplan.db.EventPlanDto
import com.partymap.backend.domain.eventplan.db.EventPlanRepository
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.place.dto.PlaceAdminListItemDto
import com.partymap.backend.domain.place.toAdminListItemDto
import com.partymap.backend.domain.user.CurrentUserService
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

}