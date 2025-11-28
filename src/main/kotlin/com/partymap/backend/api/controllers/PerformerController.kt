package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.PerformerAdminListItemDto
import com.partymap.backend.api.dtos.PerformerCreateDto
import com.partymap.backend.api.dtos.PerformerDto
import com.partymap.backend.api.mappers.toAdminListItemDto
import com.partymap.backend.api.mappers.toDto
import com.partymap.backend.api.mappers.toEntity
import com.partymap.backend.api.mappers.updateFromDto
import com.partymap.backend.domain.like.service.UserLikesFetchService
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.user.service.CurrentUserService
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
}
