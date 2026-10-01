package com.partymap.backend.domain.browse.db

import com.partymap.backend.domain.common.db.GeoPointEmbeddable
import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.search.EventSpecifications
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.Order
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import java.time.LocalDateTime
import java.util.UUID

/**
 * The distance is one expression used three times (selected, compared with the radius, ordered by), which Spring
 * Data's `Sort` cannot express, so the query is built by hand. A second query counts the matches for the page total.
 */
class BrowseEventRepositoryImpl(private val entityManager: EntityManager) : BrowseEventRepository {
    override fun browse(filter: EventBrowseFilter): Ranked<EventEntity> {
        val cb = entityManager.criteriaBuilder
        val query = cb.createTupleQuery()
        val event = query.from(EventEntity::class.java)
        val place = event.join<EventEntity, PlaceEntity>("place")
        val distance = filter.origin?.let { Haversine.km(cb, place.get("location"), it) }
        query.multiselect(event, distance ?: cb.nullLiteral(Double::class.javaObjectType))
            .where(*predicates(cb, event, place, distance, filter).toTypedArray())
            .orderBy(ordering(cb, event, distance, filter))
        val tuples = entityManager.createQuery(query)
            .setFirstResult(filter.page * filter.size)
            .setMaxResults(filter.size)
            .resultList
        val items = tuples.map { it.get(0, EventEntity::class.java) to it.get(1, Double::class.javaObjectType) }

        val countQuery = cb.createQuery(Long::class.javaObjectType)
        val countedEvent = countQuery.from(EventEntity::class.java)
        val countedPlace = countedEvent.join<EventEntity, PlaceEntity>("place")
        val countedDistance = filter.origin?.let { Haversine.km(cb, countedPlace.get("location"), it) }
        countQuery.select(cb.count(countedEvent))
            .where(*predicates(cb, countedEvent, countedPlace, countedDistance, filter).toTypedArray())
        return Ranked(items, entityManager.createQuery(countQuery).singleResult)
    }

    private fun predicates(
        cb: CriteriaBuilder,
        event: Root<EventEntity>,
        place: Join<EventEntity, PlaceEntity>,
        distance: Expression<Double>?,
        filter: EventBrowseFilter,
    ): List<Predicate> = buildList {
        add(cb.greaterThan(event.get<LocalDateTime>("end"), filter.from))
        filter.to?.let { add(cb.lessThan(event.get<LocalDateTime>("start"), it)) }
        filter.kind?.let { add(cb.equal(event.get<EventType>("kind"), it)) }
        if (filter.keywords.isNotEmpty()) add(EventSpecifications.keywordPredicate(cb, event, place, filter.keywords))
        if (filter.origin != null && filter.radiusKm != null && distance != null) {
            val location = place.get<GeoPointEmbeddable>("location")
            addAll(Haversine.withinRadius(cb, location, distance, filter.origin, filter.radiusKm))
        }
    }

    private fun ordering(
        cb: CriteriaBuilder,
        event: Root<EventEntity>,
        distance: Expression<Double>?,
        filter: EventBrowseFilter,
    ): List<Order> = buildList {
        if (filter.byDistance && distance != null) add(cb.asc(distance))
        add(cb.asc(event.get<LocalDateTime>("start")))
        add(cb.asc(event.get<UUID>("id")))
    }
}
