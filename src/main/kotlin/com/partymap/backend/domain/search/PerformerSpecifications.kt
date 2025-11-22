package com.partymap.backend.domain.search

import com.partymap.backend.domain.performer.db.PerformerEntity
import org.springframework.data.jpa.domain.Specification

object PerformerSpecifications {

    fun matchesQuery(rawQuery: String): Specification<PerformerEntity> {
        val keywords = SearchUtils.prepareKeywords(rawQuery)
        if (keywords.isEmpty()) {
            return Specification { _, _, _ -> null }
        }

        return Specification { root, _, cb ->
            val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>()

            val nameExpr = root.get<String>("name")
            val genreExpr = root.get<String>("genre")
            val bioExpr = cb.coalesce(root.get("bio"), "")

            predicates += SearchUtils.andKeywordsLike(cb, nameExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, genreExpr, keywords)
            predicates += SearchUtils.andKeywordsLike(cb, bioExpr, keywords)

            cb.or(*predicates.toTypedArray())
        }
    }
}
