package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.PlaceDto
import com.partymap.backend.api.dtos.UpcomingEventForPlaceDto
import com.partymap.backend.api.mappers.toDto
import com.partymap.backend.api.mappers.toUpcomingEventDto
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.place.db.PlaceRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api")
class PlaceController(
    private val placeRepository: PlaceRepository,
    private val eventRepository: EventRepository,
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

    @GetMapping("/places/{id}/upcoming-event")
    fun getUcomingEventForPlace(@PathVariable id: UUID): UpcomingEventForPlaceDto {
        placeRepository.findById(id)
            .orElseThrow { NoSuchElementException("Place $id not found") }

        val events =  eventRepository.findAllByPlace_Id(id)
        if (events.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No upcoming event found for place $id")
        }

        val upcoming = events
            .sortedBy { it.start }
            .firstOrNull { it.end.isAfter(Instant.now()) }
            ?: events.first()
        return upcoming.toUpcomingEventDto()
    }
}