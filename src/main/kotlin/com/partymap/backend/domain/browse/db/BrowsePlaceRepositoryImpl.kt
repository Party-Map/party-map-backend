package com.partymap.backend.domain.browse.db

import com.partymap.backend.domain.common.db.GeoPointEmbeddable
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.search.PlaceSpecifications
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Order
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import java.util.UUID

/** Same shape as [BrowseEventRepositoryImpl]: a tuple query for the page and a count query for the total. */
class BrowsePlaceRepositoryImpl(private val entityManager: EntityManager) : BrowsePlaceRepository {
    override fun browse(filter: PlaceBrowseFilter): Ranked<PlaceEntity> {
        val cb = entityManager.criteriaBuilder
        val query = cb.createTupleQuery()
        val place = query.from(PlaceEntity::class.java)
        val distance = filter.origin?.let { Haversine.km(cb, place.get("location"), it) }
        query.multiselect(place, distance ?: cb.nullLiteral(Double::class.javaObjectType))
            .where(*predicates(cb, query, place, distance, filter).toTypedArray())
            .orderBy(ordering(cb, place, distance, filter))
        val tuples = entityManager.createQuery(query)
            .setFirstResult(filter.page * filter.size)
            .setMaxResults(filter.size)
            .resultList
        val items = tuples.map { it.get(0, PlaceEntity::class.java) to it.get(1, Double::class.javaObjectType) }

        val countQuery = cb.createQuery(Long::class.javaObjectType)
        val countedPlace = countQuery.from(PlaceEntity::class.java)
        val countedDistance = filter.origin?.let { Haversine.km(cb, countedPlace.get("location"), it) }
        countQuery.select(cb.count(countedPlace))
            .where(*predicates(cb, countQuery, countedPlace, countedDistance, filter).toTypedArray())
        return Ranked(items, entityManager.createQuery(countQuery).singleResult)
    }

    private fun predicates(
        cb: CriteriaBuilder,
        query: AbstractQuery<*>,
        place: Root<PlaceEntity>,
        distance: Expression<Double>?,
        filter: PlaceBrowseFilter,
    ): List<Predicate> = buildList {
        filter.tag?.let { add(PlaceSpecifications.tagPredicate(cb, query, place, it)) }
        if (filter.keywords.isNotEmpty()) {
            add(PlaceSpecifications.keywordPredicate(cb, query, place, filter.keywords))
        }
        if (filter.origin != null && filter.radiusKm != null && distance != null) {
            val location = place.get<GeoPointEmbeddable>("location")
            addAll(Haversine.withinRadius(cb, location, distance, filter.origin, filter.radiusKm))
        }
    }

    private fun ordering(
        cb: CriteriaBuilder,
        place: Root<PlaceEntity>,
        distance: Expression<Double>?,
        filter: PlaceBrowseFilter,
    ): List<Order> = buildList {
        if (filter.byDistance && distance != null) add(cb.asc(distance))
        add(cb.asc(cb.lower(place.get<String>("name"))))
        add(cb.asc(place.get<UUID>("id")))
    }
}
