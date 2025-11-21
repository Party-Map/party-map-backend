package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.EventDto
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

    @GetMapping("/events")
    fun getEvents(
        @RequestParam(required = false) placeId: UUID?,
        @RequestParam(required = false) performerId: UUID?,
    ): List<EventDto> {
        val all = eventRepository.findAll().map { it.toDto() }

        val byPlace = placeId?.let { placeId ->
            all.filter { it.placeId == placeId }
        } ?: all

        val byPerformer = performerId?.let { performerId ->
            byPlace.filter { it.performerIds.contains(performerId) }
        } ?: byPlace

        return byPerformer
    }

    @GetMapping("/events/{id}")
    fun getEvent(@PathVariable id: UUID): EventDto =
        eventRepository.findById(id)
            .orElseThrow { NoSuchElementException("Event $id not found") }
            .toDto()
}