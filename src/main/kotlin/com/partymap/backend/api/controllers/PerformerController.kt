package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.PerformerDto
import com.partymap.backend.api.mappers.toDto
import com.partymap.backend.domain.like.service.UserLikesFetchService
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.user.service.CurrentUserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api")
class PerformerController(
    private val performerRepository: PerformerRepository,
    private val currentUserService: CurrentUserService,
    private val userLikesFetchService: UserLikesFetchService,
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
}
