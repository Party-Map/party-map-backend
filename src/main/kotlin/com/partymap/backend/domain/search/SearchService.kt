package com.partymap.backend.domain.search

import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.event.toSearchHitDto
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.performer.toSearchHitDto
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.place.toSearchHitDto
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val placeRepository: PlaceRepository,
    private val eventRepository: EventRepository,
    private val performerRepository: PerformerRepository,
) {

    fun search(rawQuery: String): SearchResponseDto {
        val query = rawQuery.trim()
        if (query.isBlank()) {
            return SearchResponseDto(
                query = query,
                hits = emptyList(),
            )
        }

        val places = placeRepository.findAll(PlaceSpecifications.matchesQuery(query))
        val events = eventRepository.findAll(EventSpecifications.matchesQuery(query))
        val performers = performerRepository.findAll(PerformerSpecifications.matchesQuery(query))

        val hits = buildList {
            addAll(
                places.map { place -> place.toSearchHitDto() },
            )
            addAll(
                events.map { event -> event.toSearchHitDto() },
            )
            addAll(
                performers.map { performer -> performer.toSearchHitDto() },
            )
        }

        return SearchResponseDto(
            query = query,
            hits = hits,
        )
    }
}
