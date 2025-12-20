package com.partymap.backend.domain.search

import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.place.db.PlaceEntity
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

object EventSpecifications {

    fun matchesQuery(rawQuery: String): Specification<EventEntity> {
        val keywords = SearchUtils.prepareKeywords(rawQuery)
        if (keywords.isEmpty()) {
            return Specification { root, _, cb ->
                cb.greaterThan(root.get("end"), LocalDateTime.now())
            }
        }

        return Specification { root, query, cb ->
            query?.distinct(true)

            val now = LocalDateTime.now()
            val futurePredicate = cb.greaterThan(root.get("end"), now)

            val placeJoin = root.join<EventEntity, PlaceEntity>("place", JoinType.LEFT)

            val predicates = mutableListOf<Predicate>()

            val titleExpr = root.get<String>("title")
            val kindExpr = root.get<String>("kind")
            val descExpr = cb.coalesce(root.get("description"), "")
            val placeNameExpr = placeJoin.get<String>("name")
            val placeCityExpr = placeJoin.get<String>("city")

            predicates += SearchUtils.andKeywordsLike(cb, titleExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, kindExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, descExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, placeNameExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, placeCityExpr, keywords)

            val anyFieldMatches = cb.or(*predicates.toTypedArray())

            cb.and(
                futurePredicate,      // only events whose end is in the future
                anyFieldMatches
            )
        }
    }
}
