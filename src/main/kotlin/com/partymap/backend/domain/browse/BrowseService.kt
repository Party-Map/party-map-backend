package com.partymap.backend.domain.browse

import com.partymap.backend.domain.browse.dto.BrowseEventsPageDto
import com.partymap.backend.domain.browse.dto.BrowsePerformersPageDto
import com.partymap.backend.domain.browse.dto.BrowsePlacesPageDto
import com.partymap.backend.domain.browse.dto.EventBrowseQuery
import com.partymap.backend.domain.browse.dto.GenreCountDto
import com.partymap.backend.domain.browse.dto.PerformerBrowseQuery
import com.partymap.backend.domain.browse.dto.PlaceBrowseQuery
import com.partymap.backend.domain.browse.dto.TagCountDto
import com.partymap.backend.domain.common.trimToNull
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.search.PerformerSpecifications
import com.partymap.backend.domain.search.SearchUtils
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

/** Paged, filtered public lists of events, places and performers, plus the facets their filter chips show. */
@Service
@Transactional(readOnly = true)
class BrowseService(
    private val eventRepository: EventRepository,
    private val placeRepository: PlaceRepository,
    private val performerRepository: PerformerRepository,
    private val clock: Clock,
) {
    fun events(query: EventBrowseQuery): BrowseEventsPageDto {
        val ranked = eventRepository.browse(query.toFilter(LocalDateTime.now(clock)))
        return BrowseEventsPageDto(
            items = ranked.items.map { (event, distanceKm) -> event.toBrowseItem(distanceKm) },
            total = ranked.total,
            page = query.page,
            size = query.size,
        )
    }

    fun places(query: PlaceBrowseQuery): BrowsePlacesPageDto {
        val ranked = placeRepository.browse(query.toFilter())
        return BrowsePlacesPageDto(
            items = ranked.items.map { (place, distanceKm) -> place.toBrowseItem(distanceKm) },
            total = ranked.total,
            page = query.page,
            size = query.size,
        )
    }

    fun performers(query: PerformerBrowseQuery): BrowsePerformersPageDto {
        var specification = Specification.unrestricted<PerformerEntity>()
        val keywords = SearchUtils.prepareKeywords(query.q.orEmpty())
        if (keywords.isNotEmpty()) specification = specification.and(PerformerSpecifications.matchesKeywords(keywords))
        query.genre.trimToNull()?.let { specification = specification.and(PerformerSpecifications.genreIs(it)) }
        val page = performerRepository.findAll(
            specification,
            PageRequest.of(query.page, query.size, Sort.by("name", "id")),
        )
        return BrowsePerformersPageDto(
            items = page.content.map { it.toBrowseItem() },
            total = page.totalElements,
            page = query.page,
            size = query.size,
        )
    }

    fun placeTags(): List<TagCountDto> = placeRepository.countTags().map { it.toDto() }

    fun performerGenres(): List<GenreCountDto> = performerRepository.countGenres().map { it.toDto() }
}
