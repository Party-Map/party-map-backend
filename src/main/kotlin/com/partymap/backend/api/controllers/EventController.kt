package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.EventDto
import com.partymap.backend.api.dtos.PerformerDto
import com.partymap.backend.api.dtos.PlaceDto
import com.partymap.backend.api.mappers.toDto
import com.partymap.backend.domain.event.db.EventRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api")
class EventController(
    private val eventRepository: EventRepository,
) {

    // /events?placeId=<UUID>
    // /events?performerId=<UUID>
    @GetMapping("/events")
    fun getEvents(
        @RequestParam(required = false) placeId: UUID?,
        @RequestParam(required = false) performerId: UUID?,
    ): List<EventDto> {
        val events = when {
            placeId != null -> eventRepository.findAllByPlace_Id(placeId)
            performerId != null -> eventRepository.findAllByPerformers_Id(performerId)
            else -> eventRepository.findAll()
        }
        return events.map { it.toDto() }
    }

    @GetMapping("/events/{id}")
    fun getEvent(@PathVariable id: UUID): EventDto =
        eventRepository.findById(id)
            .orElseThrow { NoSuchElementException("Event $id not found") }
            .toDto()

    @GetMapping("/events/{id}/place")
    fun getPlaceByEventId(@PathVariable id: UUID): PlaceDto {
        val event = eventRepository.findById(id)
            .orElseThrow { NoSuchElementException("Event $id not found") }

        return event.place.toDto()
    }

    @GetMapping("/events/{id}/performers")
    fun getPerformersByEventId(@PathVariable id: UUID): List<PerformerDto> {
        val event = eventRepository.findById(id)
            .orElseThrow { NoSuchElementException("Event $id not found") }

        return event.performers.map { it.toDto() }
    }
}