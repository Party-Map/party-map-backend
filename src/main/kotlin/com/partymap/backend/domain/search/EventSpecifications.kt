package com.partymap.backend.domain.search

import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.search.SearchUtils.contains
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

object EventSpecifications {
    /** Events that have not ended by [now] whose title, kind, description, place name or city contains each keyword. */
    fun matchesKeywords(keywords: List<String>, now: LocalDateTime): Specification<EventEntity> =
        Specification { root, _, cb ->
            val place = root.join<EventEntity, PlaceEntity>("place")
            val kind = root.get<EventType>("kind").`as`(String::class.java)
            cb.and(
                cb.greaterThan(root.get("end"), now),
                SearchUtils.everyKeywordMatches(cb, keywords) { keyword ->
                    listOf(
                        contains(cb, root.get("title"), keyword),
                        contains(cb, kind, keyword),
                        contains(cb, root.get("description"), keyword),
                        contains(cb, place.get("name"), keyword),
                        contains(cb, place.get("city"), keyword),
                    )
                },
            )
        }
}
