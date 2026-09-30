package com.partymap.backend.domain.common.db

import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventLineupItemId
import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.eventplan.db.EventPlanEntity
import com.partymap.backend.domain.eventplan.db.EventPlanLineupItemId
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationEntityId
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.user.UserEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID

/** Composite ids compare by the ids of the entities they hold, not by object identity. */
class EntityIdsTest {
    private val owner = UserEntity(UUID.randomUUID())
    private val start = LocalDateTime.of(2030, 1, 1, 20, 0)

    private fun <T : BaseEntity> T.saved(id: UUID = UUID.randomUUID()): T = apply { this.id = id }

    private fun place() = PlaceEntity(
        name = "Place",
        location = GeoPointEmbeddable(47.5, 19.0),
        address = "",
        city = "Budapest",
        description = null,
        image = null,
        tags = mutableSetOf(),
        links = mutableListOf(),
        owner = owner,
    ).saved()

    private fun performer() = PerformerEntity(
        name = "DJ",
        genre = "Techno",
        bio = "",
        image = null,
        links = mutableListOf(),
        owner = owner,
    ).saved()

    private fun event(place: PlaceEntity) = EventEntity(
        title = "E",
        place = place,
        description = "",
        start = start,
        end = start.plusHours(1),
        image = null,
        price = null,
        kind = EventType.PUB,
        links = mutableListOf(),
        owner = owner,
    ).saved()

    private fun plan() = EventPlanEntity(
        title = "P",
        description = "",
        startDateTime = start,
        endDateTime = start.plusHours(1),
        image = null,
        price = null,
        kind = EventType.PUB,
        links = mutableListOf(),
        owner = owner,
    ).saved()

    /** Checks equal and unequal pairs built by [make] from two "left" and two "right" entities. */
    private fun <L : BaseEntity, R : BaseEntity, I : Any> assertIdEquality(
        left: () -> L,
        right: () -> R,
        make: (L, R) -> I,
    ) {
        val l = left()
        val r = right()
        val lCopy = left().saved(l.id!!)
        val rCopy = right().saved(r.id!!)

        assertEquals(make(l, r), make(lCopy, rCopy))
        assertEquals(make(l, r).hashCode(), make(lCopy, rCopy).hashCode())
        assertNotEquals(make(l, r), make(left(), r))
        assertNotEquals(make(l, r), make(l, right()))
        assertFalse(make(l, r).equals("not an id"))
    }

    @Test
    fun `lineup item ids compare by event and performer id`() {
        val place = place()
        assertIdEquality({ event(place) }, ::performer, ::EventLineupItemId)
    }

    @Test
    fun `lineup invitation ids compare by plan and performer id`() {
        assertIdEquality(::plan, ::performer, ::EventPlanLineupItemId)
    }

    @Test
    fun `place invitation ids compare by plan and place id`() {
        assertIdEquality(::plan, ::place, ::EventPlanPlaceInvitationEntityId)
    }

    @Test
    fun `an unsaved entity has no required id`() {
        val unsaved = performer().apply { id = null }

        val error = assertThrows<IllegalStateException> { unsaved.requiredId }

        assertEquals("PerformerEntity is not saved yet", error.message)
    }
}
