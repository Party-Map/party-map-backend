package com.partymap.backend.domain.browse

import com.partymap.backend.domain.browse.dto.EventBrowseQuery
import com.partymap.backend.domain.browse.dto.PlaceBrowseQuery
import com.partymap.backend.domain.common.exception.InvalidRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class BrowseValidationTest {
    private val now = LocalDateTime.of(2026, 10, 1, 12, 0)

    @Test
    fun `from defaults to now and the keywords are prepared`() {
        val filter = EventBrowseQuery(q = "  Jazz  NIGHT ").toFilter(now)

        assertEquals(now, filter.from)
        assertNull(filter.to)
        assertEquals(listOf("jazz", "night"), filter.keywords)
        assertNull(filter.origin)
        assertFalse(filter.byDistance)
    }

    @Test
    fun `coordinates sort by distance unless the start order is asked for`() {
        assertTrue(EventBrowseQuery(lat = 47.5, lon = 19.0).toFilter(now).byDistance)
        assertFalse(EventBrowseQuery(lat = 47.5, lon = 19.0, sort = "start").toFilter(now).byDistance)
        assertTrue(PlaceBrowseQuery(lat = 47.5, lon = 19.0).toFilter().byDistance)
        assertFalse(PlaceBrowseQuery(lat = 47.5, lon = 19.0, sort = "name").toFilter().byDistance)
    }

    @Test
    fun `a blank tag is no tag`() {
        assertNull(PlaceBrowseQuery(tag = "  ").toFilter().tag)
        assertEquals("ruin", PlaceBrowseQuery(tag = " ruin ").toFilter().tag)
    }

    @Test
    fun `explains each inconsistency`() {
        val halfCoordinates = assertThrows<InvalidRequestException> { EventBrowseQuery(lon = 19.0).toFilter(now) }
        assertEquals("lat and lon must be given together.", halfCoordinates.message)
        val radius = assertThrows<InvalidRequestException> { PlaceBrowseQuery(radiusKm = 5.0).toFilter() }
        assertEquals("radiusKm needs lat and lon.", radius.message)
        val sort = assertThrows<InvalidRequestException> { EventBrowseQuery(sort = "distance").toFilter(now) }
        assertEquals("Sorting by distance needs lat and lon.", sort.message)
        val window = assertThrows<InvalidRequestException> {
            EventBrowseQuery(from = now.plusDays(2), to = now.plusDays(1)).toFilter(now)
        }
        assertEquals("to must not be before from.", window.message)
    }
}
