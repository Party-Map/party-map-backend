package com.partymap.backend.domain.search

import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.performer.db.PerformerEntity
import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification
import java.time.Instant

object EventSpecifications {

    fun matchesQuery(rawQuery: String): Specification<EventEntity> {
        val keywords = SearchUtils.prepareKeywords(rawQuery)
        if (keywords.isEmpty()) {
            return Specification { root, _, cb ->
                cb.greaterThan(root.get("end"), Instant.now())
            }
        }

        return Specification { root, query, cb ->
            query?.distinct(true)

            val now = Instant.now()
            val futurePredicate = cb.greaterThan(root.get("end"), now)

            val placeJoin = root.join<EventEntity, PlaceEntity>("place", JoinType.LEFT)
            val performerJoin = root.join<EventEntity, PerformerEntity>("performers", JoinType.LEFT)

            val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>()

            val titleExpr = root.get<String>("title")
            val descExpr = cb.coalesce(root.get("description"), "")
            val placeNameExpr = placeJoin.get<String>("name")
            val placeCityExpr = placeJoin.get<String>("city")
            val performerNameExpr = performerJoin.get<String>("name")
            val performerGenreExpr = performerJoin.get<String>("genre")

            predicates += SearchUtils.andKeywordsLike(cb, titleExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, descExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, placeNameExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, placeCityExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, performerNameExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, performerGenreExpr, keywords)

            val anyFieldMatches = cb.or(*predicates.toTypedArray())

            cb.and(
                futurePredicate,      // only events whose end is in the future
                anyFieldMatches
            )
        }
    }
}
