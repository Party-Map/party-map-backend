package com.partymap.backend.domain.place

import com.partymap.backend.config.Roles
import com.partymap.backend.domain.eventplan.InvitationService
import com.partymap.backend.domain.eventplan.db.InvitationAnswer
import com.partymap.backend.domain.eventplan.dto.EventPlanPlaceInvitationWithDateDto
import com.partymap.backend.domain.place.dto.PlaceAdminListItemDto
import com.partymap.backend.domain.place.dto.PlaceCreateDto
import com.partymap.backend.domain.place.dto.PlaceDto
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
@RequestMapping("/api/places")
class PlaceController(
    private val placeService: PlaceService,
    private val invitationService: InvitationService,
    private val currentUserService: CurrentUserService,
) {
    @GetMapping
    fun getPlaces(
        @Parameter(description = "Only places inside minLon,minLat,maxLon,maxLat", example = "18.9,47.3,19.3,47.7")
        @RequestParam(required = false)
        bbox: String?,
    ): List<PlaceDto> = placeService.list(bbox?.let(BoundingBox::parse))

    @GetMapping("/{id}")
    fun getPlace(@PathVariable id: UUID): PlaceDto = placeService.get(id)

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/liked-places")
    fun getLikedPlaces(@AuthenticationPrincipal jwt: Jwt): List<PlaceDto> =
        placeService.liked(currentUserService.getOrCreateUser(jwt).sub)

    @PreAuthorize("hasRole('${Roles.PLACE_MANAGER}')")
    @GetMapping("/owned-places")
    fun getOwnedPlaces(@AuthenticationPrincipal jwt: Jwt): List<PlaceAdminListItemDto> =
        placeService.owned(currentUserService.getOrCreateUser(jwt).sub)

    @PreAuthorize("hasRole('${Roles.PLACE_MANAGER}')")
    @PostMapping
    fun createPlace(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody dto: PlaceCreateDto): PlaceDto =
        placeService.create(currentUserService.getOrCreateUser(jwt).sub, dto)

    @PreAuthorize("hasRole('${Roles.PLACE_MANAGER}')")
    @PutMapping("/{id}")
    fun updatePlace(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @Valid @RequestBody dto: PlaceCreateDto,
    ): PlaceDto = placeService.update(currentUserService.getOrCreateUser(jwt).sub, id, dto)

    @PreAuthorize("hasRole('${Roles.PLACE_MANAGER}')")
    @GetMapping("/{id}/invitations")
    fun getPlaceInvitations(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
    ): List<EventPlanPlaceInvitationWithDateDto> =
        invitationService.placeInvitations(currentUserService.getOrCreateUser(jwt).sub, id)

    @PreAuthorize("hasRole('${Roles.PLACE_MANAGER}')")
    @PutMapping("/{id}/invitations/{eventPlanId}/respond")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun respondToPlaceInvitation(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @PathVariable eventPlanId: UUID,
        @Parameter(description = "accept or reject") @RequestParam state: String,
    ) {
        val answer = InvitationAnswer.parse(state)
        invitationService.respondAsPlace(currentUserService.getOrCreateUser(jwt).sub, id, eventPlanId, answer)
    }
}
