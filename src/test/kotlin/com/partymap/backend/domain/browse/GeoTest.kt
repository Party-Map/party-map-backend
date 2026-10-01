package com.partymap.backend.domain.browse

import com.partymap.backend.domain.browse.db.GeoOrigin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GeoTest {
    @Test
    fun `the box around Budapest grows with the radius and is wider in longitude than in latitude`() {
        val box = GeoOrigin(47.5, 19.0).boundingBox(50.0)

        assertEquals(47.5 - 50.0 / 111.32, box.minLatitude, 1e-9)
        assertEquals(47.5 + 50.0 / 111.32, box.maxLatitude, 1e-9)
        val longitudeHalfWidth = (box.maxLongitude - box.minLongitude) / 2
        assertTrue(longitudeHalfWidth > 50.0 / 111.32, "degrees of longitude are shorter at 47.5°")
        assertEquals(19.0, (box.minLongitude + box.maxLongitude) / 2, 1e-9)
    }

    @Test
    fun `near the poles the box spans every longitude and the latitude is clamped`() {
        val box = GeoOrigin(89.5, 10.0).boundingBox(100.0)

        assertEquals(-180.0, box.minLongitude)
        assertEquals(180.0, box.maxLongitude)
        assertEquals(90.0, box.maxLatitude)
    }

    @Test
    fun `longitudes are clamped at the antimeridian`() {
        val box = GeoOrigin(0.0, 179.9).boundingBox(100.0)

        assertEquals(180.0, box.maxLongitude)
        assertTrue(box.minLongitude < 179.9)
    }
}
