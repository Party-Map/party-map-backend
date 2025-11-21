package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.PerformerDto
import com.partymap.backend.api.mappers.toDto
import com.partymap.backend.domain.performer.db.PerformerRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api")
class PerformerController(
    private val performerRepository: PerformerRepository,
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
}
