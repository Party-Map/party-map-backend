package com.partymap.backend.domain.search

import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.search.SearchUtils.contains
import org.springframework.data.jpa.domain.Specification

object PerformerSpecifications {
    /** Name, genre or bio contains each keyword. */
    fun matchesKeywords(keywords: List<String>): Specification<PerformerEntity> = Specification { root, _, cb ->
        SearchUtils.everyKeywordMatches(cb, keywords) { keyword ->
            listOf(
                contains(cb, root.get("name"), keyword),
                contains(cb, root.get("genre"), keyword),
                contains(cb, root.get("bio"), keyword),
            )
        }
    }
}
