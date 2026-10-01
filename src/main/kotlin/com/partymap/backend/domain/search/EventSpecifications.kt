package com.partymap.backend.domain.search

import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.search.SearchUtils.contains
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

object EventSpecifications {
    /** Events that have not ended by [now] whose title, kind, description, place name or city contains each keyword. */
    fun matchesKeywords(keywords: List<String>, now: LocalDateTime): Specification<EventEntity> =
        Specification { root, _, cb ->
            val place = root.join<EventEntity, PlaceEntity>("place")
            cb.and(cb.greaterThan(root.get("end"), now), keywordPredicate(cb, root, place, keywords))
        }

    /** Title, kind, description, place name or city contains each keyword ([place] is the event's joined place). */
    fun keywordPredicate(
        cb: CriteriaBuilder,
        event: Path<EventEntity>,
        place: Path<PlaceEntity>,
        keywords: List<String>,
    ): Predicate {
        val kind = event.get<EventType>("kind").`as`(String::class.java)
        return SearchUtils.everyKeywordMatches(cb, keywords) { keyword ->
            listOf(
                contains(cb, event.get("title"), keyword),
                contains(cb, kind, keyword),
                contains(cb, event.get("description"), keyword),
                contains(cb, place.get("name"), keyword),
                contains(cb, place.get("city"), keyword),
            )
        }
    }
}
