package com.partymap.backend.domain.search

import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.event.toSearchHitDto
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.performer.toSearchHitDto
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.place.toSearchHitDto
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

/** Free-text search over places, upcoming events and performers; hits come in that order. */
@Service
@Transactional(readOnly = true)
class SearchService(
    private val placeRepository: PlaceRepository,
    private val eventRepository: EventRepository,
    private val performerRepository: PerformerRepository,
    private val clock: Clock,
) {
    fun search(rawQuery: String): SearchResponseDto {
        val query = rawQuery.trim()
        val keywords = SearchUtils.prepareKeywords(query)
        if (keywords.isEmpty()) return SearchResponseDto(query = query, hits = emptyList())

        val places = placeRepository.findAll(PlaceSpecifications.matchesKeywords(keywords), Sort.by("name"))
        val events = eventRepository.findAll(
            EventSpecifications.matchesKeywords(keywords, LocalDateTime.now(clock)),
            Sort.by("start"),
        )
        val performers = performerRepository.findAll(PerformerSpecifications.matchesKeywords(keywords), Sort.by("name"))
        val hits = places.map { it.toSearchHitDto() } +
            events.map { it.toSearchHitDto() } +
            performers.map { it.toSearchHitDto() }
        return SearchResponseDto(query = query, hits = hits)
    }
}
