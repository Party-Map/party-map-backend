package com.partymap.backend.domain.performer

import com.partymap.backend.domain.eventplan.EventPlanService
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.eventplan.db.EventPlanRepository
import com.partymap.backend.domain.eventplan.dto.EventPlanLineupInvitationForPerformerDto
import com.partymap.backend.domain.eventplan.toForPerformerDto
import com.partymap.backend.domain.like.service.UserLikesFetchService
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.performer.dto.PerformerAdminListItemDto
import com.partymap.backend.domain.performer.dto.PerformerCreateDto
import com.partymap.backend.domain.performer.dto.PerformerDto
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
class PerformerController(
    private val performerRepository: PerformerRepository,
    private val currentUserService: CurrentUserService,
    private val userLikesFetchService: UserLikesFetchService,
    private val eventPlanRepository: EventPlanRepository,
    private val eventPlanService: EventPlanService,
) {

    @GetMapping("/performers")
    fun getPerformers(): List<PerformerDto> =
        performerRepository.findAll()
            .map { it.toDto() }

    @GetMapping("/performers/{id}")
    fun getPerformer(@PathVariable id: UUID): PerformerDto =
        performerRepository.findById(id)
            .orElseThrow { NoSuchElementException("Performer $id not found") }
            .toDto()

    @GetMapping("/performers/liked-performers")
    fun getLikedPerformersForUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<PerformerDto> {
        val user = currentUserService.getOrCreateUser(jwt)
        return userLikesFetchService.getLikedPerformers(user.sub)
    }

    @PreAuthorize("hasRole('performer_manager_user')")
    @GetMapping("/performers/owned-performers")
    fun getOwnedPerformersForUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<PerformerAdminListItemDto> {
        val user = currentUserService.getOrCreateUser(jwt)
        return performerRepository.findAllByOwner_Sub(user.sub).map { it.toAdminListItemDto() }
    }

    @PreAuthorize("hasRole('performer_manager_user')")
    @PostMapping("/performers")
    fun createPlace(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody dto: PerformerCreateDto,
    ): PerformerDto {
        val user = currentUserService.getOrCreateUser(jwt)
        val entity = dto.toEntity(user)
        val saved = performerRepository.save(entity)
        return saved.toDto()
    }

    @PreAuthorize("hasRole('place_manager_user')")
    @PutMapping("/performers/{id}")
    fun updatePlace(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @RequestBody dto: PerformerCreateDto,
    ): PerformerDto {
        val user = currentUserService.getOrCreateUser(jwt)

        val performer = performerRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Performer $id not found") }

        if (performer.owner.sub != user.sub) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this performer")
        }

        performer.updateFromDto(dto)

        val saved = performerRepository.save(performer)
        return saved.toDto()
    }

    @PreAuthorize("hasRole('performer_manager_user')")
    @GetMapping("/performers/{id}/invitations")
    fun getInvitationsForPerformer(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
    ): List<EventPlanLineupInvitationForPerformerDto> {
        val user = currentUserService.getOrCreateUser(jwt)

        val performer = performerRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Performer $id not found") }

        if (performer.owner.sub != user.sub) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to view invitations for this performer"
            )
        }

        val eventPlans = eventPlanRepository.findAll()

        return eventPlans.flatMap { eventPlan ->
            eventPlan.lineupInvitations
                .filter { it.id.performer.id == performer.id }
                .map { it.toForPerformerDto() }
        }
    }

    @PreAuthorize("hasRole('performer_manager_user')")
    @PutMapping("/performers/{id}/invitations/{eventPlanId}/respond")
    fun respondToInvitation(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @PathVariable eventPlanId: UUID,
        @RequestParam state: String,
    ): HttpStatus {
        val user = currentUserService.getOrCreateUser(jwt)

        val performer = performerRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Performer $id not found") }

        if (performer.owner.sub != user.sub) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to respond to invitations for this performer"
            )
        }

        val newState = when (state.lowercase()) {
            "accept" -> EventPlanLineupInvitationState.ACCEPTED
            "reject" -> EventPlanLineupInvitationState.REJECTED
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state: $state")
        }

        val eventPlan = eventPlanRepository.findById(eventPlanId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Event plan $eventPlanId not found") }

        try {
            eventPlanService.respondToPerformerInvitation(eventPlan, performer, newState)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message ?: "Invitation not found")
        }

        return HttpStatus.OK
    }
}