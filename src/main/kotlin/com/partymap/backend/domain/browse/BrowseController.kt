package com.partymap.backend.domain.browse

import com.partymap.backend.domain.browse.dto.BrowseEventsPageDto
import com.partymap.backend.domain.browse.dto.BrowsePerformersPageDto
import com.partymap.backend.domain.browse.dto.BrowsePlacesPageDto
import com.partymap.backend.domain.browse.dto.EventBrowseQuery
import com.partymap.backend.domain.browse.dto.GenreCountDto
import com.partymap.backend.domain.browse.dto.PerformerBrowseQuery
import com.partymap.backend.domain.browse.dto.PlaceBrowseQuery
import com.partymap.backend.domain.browse.dto.TagCountDto
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Public browse lists: paged and filtered, sorted by distance from the caller when coordinates are given. */
@RestController
@RequestMapping("/api/browse")
class BrowseController(private val browseService: BrowseService) {
    @GetMapping("/events")
    fun events(@Valid @ParameterObject @ModelAttribute query: EventBrowseQuery): BrowseEventsPageDto =
        browseService.events(query)

    @GetMapping("/places")
    fun places(@Valid @ParameterObject @ModelAttribute query: PlaceBrowseQuery): BrowsePlacesPageDto =
        browseService.places(query)

    @GetMapping("/performers")
    fun performers(@Valid @ParameterObject @ModelAttribute query: PerformerBrowseQuery): BrowsePerformersPageDto =
        browseService.performers(query)

    @GetMapping("/place-tags")
    fun placeTags(): List<TagCountDto> = browseService.placeTags()

    @GetMapping("/performer-genres")
    fun performerGenres(): List<GenreCountDto> = browseService.performerGenres()
}
