package com.partymap.backend.support

import com.partymap.backend.domain.common.db.GeoPointEmbeddable
import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.common.db.LinkType
import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventLineupItemEntity
import com.partymap.backend.domain.event.db.EventLineupItemId
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.eventplan.db.EventPlanEntity
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationEntity
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.eventplan.db.EventPlanLineupItemId
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationEntity
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationEntityId
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationState
import com.partymap.backend.domain.eventplan.db.EventPlanRepository
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.user.UserEntity
import com.partymap.backend.domain.user.UserRepository
import org.springframework.boot.test.context.TestComponent
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

/** Factories for committed test rows; every argument has a sensible default. */
@TestComponent
class TestData(
    private val users: UserRepository,
    private val places: PlaceRepository,
    private val performers: PerformerRepository,
    private val events: EventRepository,
    private val eventPlans: EventPlanRepository,
    private val transaction: TransactionTemplate,
) {
    /** Now, truncated to seconds so values survive the JSON round trip unchanged. */
    val now: LocalDateTime get() = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

    fun user(sub: UUID = UUID.randomUUID()): UserEntity = users.save(UserEntity(sub = sub))

    fun place(
        owner: UserEntity = user(),
        name: String = "Akvárium Klub",
        latitude: Double = 47.4979,
        longitude: Double = 19.0402,
        city: String = "Budapest",
        address: String = "Erzsébet tér 12",
        description: String? = "Club under the pond",
        image: String? = null,
        tags: Set<String> = setOf("club"),
        links: List<LinkEmbeddable> = listOf(LinkEmbeddable(LinkType.WEBSITE, "https://akvariumklub.hu")),
    ): PlaceEntity = places.save(
        PlaceEntity(
            name = name,
            location = GeoPointEmbeddable(latitude, longitude),
            address = address,
            city = city,
            description = description,
            image = image,
            tags = tags.toMutableSet(),
            links = links.toMutableList(),
            owner = owner,
        ),
    )

    fun performer(
        owner: UserEntity = user(),
        name: String = "DJ Pond",
        genre: String = "Techno",
        bio: String = "Plays until sunrise",
    ): PerformerEntity = performers.save(
        PerformerEntity(
            name = name,
            genre = genre,
            bio = bio,
            image = null,
            links = mutableListOf(LinkEmbeddable(LinkType.INSTAGRAM, "https://instagram.com/djpond")),
            owner = owner,
        ),
    )

    fun event(
        place: PlaceEntity,
        owner: UserEntity = user(),
        title: String = "Pond Party",
        start: LocalDateTime = now.plusDays(1),
        end: LocalDateTime = start.plusHours(5),
        kind: EventType = EventType.TECHNO,
        lineup: List<PerformerEntity> = emptyList(),
        image: String? = null,
    ): EventEntity = transaction.execute {
        val event = events.save(
            EventEntity(
                title = title,
                place = place,
                description = "An evening at the pond",
                start = start,
                end = end,
                image = image,
                price = "3000 HUF",
                kind = kind,
                links = mutableListOf(LinkEmbeddable(LinkType.FACEBOOK, "https://facebook.com/pondparty")),
                owner = owner,
            ),
        )
        lineup.forEach { performer ->
            event.lineupItems.add(EventLineupItemEntity(EventLineupItemId(event, performer), start, end))
        }
        events.save(event)
    }!!

    fun eventPlan(
        owner: UserEntity = user(),
        title: String = "Summer Opening",
        start: LocalDateTime = now.plusDays(7),
        end: LocalDateTime = start.plusHours(6),
    ): EventPlanEntity = eventPlans.save(
        EventPlanEntity(
            title = title,
            description = "Opening night",
            startDateTime = start,
            endDateTime = end,
            image = null,
            price = "Free",
            kind = EventType.FESTIVAL,
            links = mutableListOf(LinkEmbeddable(LinkType.WEBSITE, "https://summer.example")),
            owner = owner,
        ),
    )

    /** Adds a place invitation in [state] to [plan]. */
    fun placeInvitation(
        plan: EventPlanEntity,
        place: PlaceEntity,
        state: EventPlanPlaceInvitationState = EventPlanPlaceInvitationState.PENDING,
    ) {
        transaction.executeWithoutResult {
            val managed = eventPlans.findById(plan.id!!).orElseThrow()
            managed.placeInvitations.add(
                EventPlanPlaceInvitationEntity(EventPlanPlaceInvitationEntityId(managed, place), state),
            )
        }
    }

    /** Adds a lineup invitation in [state] to [plan], by default for the plan's first hour. */
    fun lineupInvitation(
        plan: EventPlanEntity,
        performer: PerformerEntity,
        state: EventPlanLineupInvitationState = EventPlanLineupInvitationState.PENDING,
        start: LocalDateTime = plan.startDateTime,
        end: LocalDateTime = start.plusHours(1),
    ) {
        transaction.executeWithoutResult {
            val managed = eventPlans.findById(plan.id!!).orElseThrow()
            managed.lineupInvitations.add(
                EventPlanLineupInvitationEntity(EventPlanLineupItemId(managed, performer), state, start, end),
            )
        }
    }
}
