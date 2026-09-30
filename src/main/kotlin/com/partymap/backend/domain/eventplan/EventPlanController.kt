package com.partymap.backend.domain.eventplan

import com.partymap.backend.config.Roles
import com.partymap.backend.domain.eventplan.dto.EventPlanAdminListItemDto
import com.partymap.backend.domain.eventplan.dto.EventPlanCreateDto
import com.partymap.backend.domain.eventplan.dto.EventPlanDto
import com.partymap.backend.domain.eventplan.dto.EventPlanLineupInvitationCreatePayloadDto
import com.partymap.backend.domain.eventplan.dto.EventPlanLineupInvitationDto
import com.partymap.backend.domain.place.PlaceService
import com.partymap.backend.domain.place.dto.PlaceAdminListItemDto
import com.partymap.backend.domain.user.CurrentUserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/** Event plans (drafts) of the calling organizer. */
@RestController
@RequestMapping("/api/event-plan")
@PreAuthorize("hasRole('${Roles.EVENT_ORGANIZER}')")
class EventPlanController(
    private val eventPlanService: EventPlanService,
    private val placeService: PlaceService,
    private val currentUserService: CurrentUserService,
) {
    @GetMapping("/{id}")
    fun getEventPlan(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: UUID): EventPlanDto =
        eventPlanService.get(sub(jwt), id)

    @GetMapping("/places")
    fun getPlacesToInvite(): List<PlaceAdminListItemDto> = placeService.adminList()

    @GetMapping("/owned-event-plans")
    fun getOwnedEventPlans(@AuthenticationPrincipal jwt: Jwt): List<EventPlanAdminListItemDto> =
        eventPlanService.owned(sub(jwt))

    @PostMapping
    fun createEventPlan(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody dto: EventPlanCreateDto): EventPlanDto =
        eventPlanService.create(sub(jwt), dto)

    @PutMapping("/{id}")
    fun updateEventPlan(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @Valid @RequestBody dto: EventPlanCreateDto,
    ): EventPlanDto = eventPlanService.update(sub(jwt), id, dto)

    @PutMapping("/{id}/invite-place/{placeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun invitePlace(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: UUID, @PathVariable placeId: UUID) =
        eventPlanService.invitePlace(sub(jwt), id, placeId)

    @GetMapping("/{id}/lineup-invitations")
    fun getLineupInvitations(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
    ): List<EventPlanLineupInvitationDto> = eventPlanService.lineupInvitations(sub(jwt), id)

    @PostMapping("/{id}/add-lineup-invitation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addLineupInvitation(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @Valid @RequestBody dto: EventPlanLineupInvitationCreatePayloadDto,
    ) = eventPlanService.invitePerformer(sub(jwt), id, dto)

    @DeleteMapping("/{id}/lineup-invitation/{performerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLineupInvitation(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @PathVariable performerId: UUID,
    ) = eventPlanService.removePerformer(sub(jwt), id, performerId)

    @PostMapping("/{id}/publish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun publishEventPlan(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: UUID) {
        eventPlanService.publish(sub(jwt), id)
    }

    private fun sub(jwt: Jwt): UUID = currentUserService.getOrCreateUser(jwt).sub
}
