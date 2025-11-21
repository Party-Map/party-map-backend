package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.PlaceDto
import com.partymap.backend.api.mappers.toDto
import com.partymap.backend.domain.place.db.PlaceRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api")
class PlaceController(
    private val placeRepository: PlaceRepository,
) {
    @GetMapping("/places")
    fun getPlaces(): List<PlaceDto> =
        placeRepository.findAll()
            .map { it.toDto() }

    @GetMapping("/places/{id}")
    fun getPlace(@PathVariable id: UUID): PlaceDto =
        placeRepository.findById(id)
            .orElseThrow { NoSuchElementException("Place $id not found") }
            .toDto()
}