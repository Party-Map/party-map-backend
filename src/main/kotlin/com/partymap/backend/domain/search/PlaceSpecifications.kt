package com.partymap.backend.domain.search

import com.partymap.backend.domain.place.db.PlaceEntity
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object PlaceSpecifications {

    fun matchesQuery(rawQuery: String): Specification<PlaceEntity> {
        val keywords = SearchUtils.prepareKeywords(rawQuery)
        if (keywords.isEmpty()) {
            return Specification { _, _, _ -> null }
        }

        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            val nameExpr = root.get<String>("name")
            val cityExpr = root.get<String>("city")
            val addressExpr = root.get<String>("address")
            val descExpr = cb.coalesce(root.get("description"), "")

            predicates += SearchUtils.andKeywordsLike(cb, nameExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, cityExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, addressExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, descExpr, keywords)

            val tagsJoin = root.join<PlaceEntity, String>("tags", JoinType.LEFT)
            predicates += SearchUtils.andKeywordsLike(cb, tagsJoin, keywords)

            cb.or(*predicates.toTypedArray())
        }
    }
}
