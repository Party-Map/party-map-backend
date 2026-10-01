package com.partymap.backend.domain.search

import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.search.SearchUtils.contains
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

object PlaceSpecifications {
    /** Name, city, address, description or one of the tags contains each keyword. */
    fun matchesKeywords(keywords: List<String>): Specification<PlaceEntity> = Specification { root, query, cb ->
        keywordPredicate(cb, requireNotNull(query), root, keywords)
    }

    /** The same rule as a predicate for hand-built criteria queries ([query] owns the tag subqueries). */
    fun keywordPredicate(
        cb: CriteriaBuilder,
        query: AbstractQuery<*>,
        root: Root<PlaceEntity>,
        keywords: List<String>,
    ): Predicate {
        val description = cb.coalesce(root.get<String>("description"), "")
        return SearchUtils.everyKeywordMatches(cb, keywords) { keyword ->
            val tagQuery = query.subquery(Int::class.java)
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

    /** One of the place's tags equals [tag], ignoring case. */
    fun tagPredicate(cb: CriteriaBuilder, query: AbstractQuery<*>, root: Root<PlaceEntity>, tag: String): Predicate {
        val tagQuery = query.subquery(Int::class.java)
        val tagPath = tagQuery.correlate(root).join<PlaceEntity, String>("tags")
        tagQuery.select(cb.literal(1)).where(cb.equal(cb.lower(tagPath), tag.lowercase()))
        return cb.exists(tagQuery)
    }
}
