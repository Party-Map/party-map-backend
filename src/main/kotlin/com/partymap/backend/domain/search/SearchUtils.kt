package com.partymap.backend.domain.search

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate

object SearchUtils {
    const val ESCAPE = '\\'

    /** Lower-cased words of the query. */
    fun prepareKeywords(rawQuery: String): List<String> = rawQuery.trim()
        .lowercase()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    /** A `LIKE` pattern matching [keyword] anywhere, with `%`, `_` and the escape character taken literally. */
    fun containsPattern(keyword: String): String {
        val escaped = keyword
            .replace("$ESCAPE", "$ESCAPE$ESCAPE")
            .replace("%", "$ESCAPE%")
            .replace("_", "${ESCAPE}_")
        return "%$escaped%"
    }

    /** Case-insensitive "[expression] contains [keyword]". */
    fun contains(cb: CriteriaBuilder, expression: Expression<String>, keyword: String): Predicate =
        cb.like(cb.lower(expression), containsPattern(keyword), ESCAPE)

    /** Every keyword has to match at least one of the fields ([fieldMatches] builds the per-keyword alternatives). */
    fun everyKeywordMatches(
        cb: CriteriaBuilder,
        keywords: List<String>,
        fieldMatches: (String) -> List<Predicate>,
    ): Predicate = cb.and(*keywords.map { cb.or(*fieldMatches(it).toTypedArray()) }.toTypedArray())
}
