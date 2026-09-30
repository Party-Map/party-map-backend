package com.partymap.backend.domain.search

import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.search.SearchUtils.contains
import org.springframework.data.jpa.domain.Specification

object PlaceSpecifications {
    /** Name, city, address, description or one of the tags contains each keyword. */
    fun matchesKeywords(keywords: List<String>): Specification<PlaceEntity> = Specification { root, query, cb ->
        val description = cb.coalesce(root.get<String>("description"), "")
        SearchUtils.everyKeywordMatches(cb, keywords) { keyword ->
            val tagQuery = requireNotNull(query).subquery(Int::class.java)
            val tag = tagQuery.correlate(root).join<PlaceEntity, String>("tags")
            tagQuery.select(cb.literal(1)).where(contains(cb, tag, keyword))
            listOf(
                contains(cb, root.get("name"), keyword),
                contains(cb, root.get("city"), keyword),
                contains(cb, root.get("address"), keyword),
                contains(cb, description, keyword),
                cb.exists(tagQuery),
            )
        }
    }
}
