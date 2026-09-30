package com.partymap.backend.domain.like

import com.partymap.backend.domain.like.dto.LikeStatusDto
import com.partymap.backend.domain.user.CurrentUserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/** The caller's likes. PUT and DELETE are idempotent and answer with the stored state. */
@RestController
@RequestMapping("/api/me/likes")
@PreAuthorize("isAuthenticated()")
class LikeController(private val likeService: LikeService, private val currentUserService: CurrentUserService) {
    @GetMapping("/events/{eventId}")
    fun isEventLiked(@PathVariable eventId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        status(jwt, LikeTarget.EVENT, eventId)

    @PutMapping("/events/{eventId}")
    fun likeEvent(@PathVariable eventId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        set(jwt, LikeTarget.EVENT, eventId, liked = true)

    @DeleteMapping("/events/{eventId}")
    fun unlikeEvent(@PathVariable eventId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        set(jwt, LikeTarget.EVENT, eventId, liked = false)

    @GetMapping("/places/{placeId}")
    fun isPlaceLiked(@PathVariable placeId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        status(jwt, LikeTarget.PLACE, placeId)

    @PutMapping("/places/{placeId}")
    fun likePlace(@PathVariable placeId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        set(jwt, LikeTarget.PLACE, placeId, liked = true)

    @DeleteMapping("/places/{placeId}")
    fun unlikePlace(@PathVariable placeId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        set(jwt, LikeTarget.PLACE, placeId, liked = false)

    @GetMapping("/performers/{performerId}")
    fun isPerformerLiked(@PathVariable performerId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        status(jwt, LikeTarget.PERFORMER, performerId)

    @PutMapping("/performers/{performerId}")
    fun likePerformer(@PathVariable performerId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        set(jwt, LikeTarget.PERFORMER, performerId, liked = true)

    @DeleteMapping("/performers/{performerId}")
    fun unlikePerformer(@PathVariable performerId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        set(jwt, LikeTarget.PERFORMER, performerId, liked = false)

    private fun status(jwt: Jwt, target: LikeTarget, id: UUID) =
        LikeStatusDto(likeService.isLiked(currentUserService.getOrCreateUser(jwt).sub, target, id))

    private fun set(jwt: Jwt, target: LikeTarget, id: UUID, liked: Boolean) =
        LikeStatusDto(likeService.setLiked(currentUserService.getOrCreateUser(jwt).sub, target, id, liked))
}
