package com.partymap.backend.domain.performer

import com.partymap.backend.config.Roles
import com.partymap.backend.domain.eventplan.InvitationService
import com.partymap.backend.domain.eventplan.db.InvitationAnswer
import com.partymap.backend.domain.eventplan.dto.EventPlanLineupInvitationForPerformerDto
import com.partymap.backend.domain.performer.dto.PerformerAdminListItemDto
import com.partymap.backend.domain.performer.dto.PerformerCreateDto
import com.partymap.backend.domain.performer.dto.PerformerDto
import com.partymap.backend.domain.user.CurrentUserService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/performers")
class PerformerController(
    private val performerService: PerformerService,
    private val invitationService: InvitationService,
    private val currentUserService: CurrentUserService,
) {
    @GetMapping
    fun getPerformers(): List<PerformerDto> = performerService.list()

    @GetMapping("/{id}")
    fun getPerformer(@PathVariable id: UUID): PerformerDto = performerService.get(id)

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/liked-performers")
    fun getLikedPerformers(@AuthenticationPrincipal jwt: Jwt): List<PerformerDto> =
        performerService.liked(currentUserService.getOrCreateUser(jwt).sub)

    @PreAuthorize("hasRole('${Roles.PERFORMER_MANAGER}')")
    @GetMapping("/owned-performers")
    fun getOwnedPerformers(@AuthenticationPrincipal jwt: Jwt): List<PerformerAdminListItemDto> =
        performerService.owned(currentUserService.getOrCreateUser(jwt).sub)

    @PreAuthorize("hasRole('${Roles.PERFORMER_MANAGER}')")
    @PostMapping
    fun createPerformer(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody dto: PerformerCreateDto): PerformerDto =
        performerService.create(currentUserService.getOrCreateUser(jwt).sub, dto)

    @PreAuthorize("hasRole('${Roles.PERFORMER_MANAGER}')")
    @PutMapping("/{id}")
    fun updatePerformer(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @Valid @RequestBody dto: PerformerCreateDto,
    ): PerformerDto = performerService.update(currentUserService.getOrCreateUser(jwt).sub, id, dto)

    @PreAuthorize("hasRole('${Roles.PERFORMER_MANAGER}')")
    @GetMapping("/{id}/invitations")
    fun getPerformerInvitations(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
    ): List<EventPlanLineupInvitationForPerformerDto> =
        invitationService.performerInvitations(currentUserService.getOrCreateUser(jwt).sub, id)

    @PreAuthorize("hasRole('${Roles.PERFORMER_MANAGER}')")
    @PutMapping("/{id}/invitations/{eventPlanId}/respond")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun respondToPerformerInvitation(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @PathVariable eventPlanId: UUID,
        @Parameter(description = "accept or reject") @RequestParam state: String,
    ) {
        val answer = InvitationAnswer.parse(state)
        invitationService.respondAsPerformer(currentUserService.getOrCreateUser(jwt).sub, id, eventPlanId, answer)
    }
}
