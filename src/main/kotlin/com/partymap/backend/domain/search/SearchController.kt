package com.partymap.backend.domain.search

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class SearchController(private val searchService: SearchService) {

    @GetMapping("/search")
    fun search(@RequestParam q: String): SearchResponseDto =
        searchService.search(q)

}