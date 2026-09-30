package com.partymap.backend.domain.search

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate

object SearchUtils {

    fun prepareKeywords(rawQuery: String): List<String> = rawQuery.trim()
        .lowercase()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    fun andKeywordsLike(cb: CriteriaBuilder, expression: Expression<String>, keywords: List<String>): Predicate {
        val preds = keywords.map { kw ->
            cb.like(cb.lower(expression), "%$kw%")
        }
        return cb.and(*preds.toTypedArray())
    }
}
