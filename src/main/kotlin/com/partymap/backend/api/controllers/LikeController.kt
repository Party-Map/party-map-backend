package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.LikeStatusDto
import com.partymap.backend.domain.like.service.EventLikeService
import com.partymap.backend.domain.like.service.PerformerLikeService
import com.partymap.backend.domain.like.service.PlaceLikeService
import com.partymap.backend.domain.user.service.CurrentUserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/me/likes")
class LikeController(
    private val eventLikeService: EventLikeService,
    private val placeLikeService: PlaceLikeService,
    private val performerLikeService: PerformerLikeService,
    private val currentUserService: CurrentUserService,
) {

    @GetMapping("/events/{eventId}")
    fun isEventLiked(
        @PathVariable eventId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): LikeStatusDto {
        val user = currentUserService.getOrCreateUser(jwt)
        return LikeStatusDto(eventLikeService.isLiked(user, eventId))
    }

    @PutMapping("/events/{eventId}")
    fun likeEvent(
        @PathVariable eventId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): LikeStatusDto {
        val user = currentUserService.getOrCreateUser(jwt)
        eventLikeService.like(user, eventId)
        return LikeStatusDto(liked = true)
    }

    @DeleteMapping("/events/{eventId}")
    fun unlikeEvent(
        @PathVariable eventId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): LikeStatusDto {
        val user = currentUserService.getOrCreateUser(jwt)
        eventLikeService.unlike(user, eventId)
        return LikeStatusDto(liked = false)
    }


    @GetMapping("/places/{placeId}")
    fun isPlaceLiked(
        @PathVariable placeId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): LikeStatusDto {
        val user = currentUserService.getOrCreateUser(jwt)
        return LikeStatusDto(placeLikeService.isLiked(user, placeId))
    }

    @PutMapping("/places/{placeId}")
    fun likePlace(
        @PathVariable placeId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): LikeStatusDto {
        val user = currentUserService.getOrCreateUser(jwt)
        placeLikeService.like(user, placeId)
        return LikeStatusDto(liked = true)
    }

    @DeleteMapping("/places/{placeId}")
    fun unlikePlace(
        @PathVariable placeId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): LikeStatusDto {
        val user = currentUserService.getOrCreateUser(jwt)
        placeLikeService.unlike(user, placeId)
        return LikeStatusDto(liked = false)
    }


    @GetMapping("/performers/{performerId}")
    fun isPerformerLiked(
        @PathVariable performerId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): LikeStatusDto {
        val user = currentUserService.getOrCreateUser(jwt)
        return LikeStatusDto(performerLikeService.isLiked(user, performerId))
    }

    @PutMapping("/performers/{performerId}")
    fun likePerformer(
        @PathVariable performerId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): LikeStatusDto {
        val user = currentUserService.getOrCreateUser(jwt)
        performerLikeService.like(user, performerId)
        return LikeStatusDto(liked = true)
    }

    @DeleteMapping("/performers/{performerId}")
    fun unlikePerformer(
        @PathVariable performerId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): LikeStatusDto {
        val user = currentUserService.getOrCreateUser(jwt)
        performerLikeService.unlike(user, performerId)
        return LikeStatusDto(liked = false)
    }
}
