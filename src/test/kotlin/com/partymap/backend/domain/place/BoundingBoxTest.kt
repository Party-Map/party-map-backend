package com.partymap.backend.domain.place

import com.partymap.backend.domain.common.exception.InvalidRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BoundingBoxTest {
    @Test
    fun `parses minLon,minLat,maxLon,maxLat with spaces`() {
        assertEquals(BoundingBox(18.9, 47.3, 19.3, 47.7), BoundingBox.parse("18.9, 47.3 ,19.3,47.7"))
    }

    @Test
    fun `accepts the whole world and a single point`() {
        assertEquals(BoundingBox(-180.0, -90.0, 180.0, 90.0), BoundingBox.parse("-180,-90,180,90"))
        assertEquals(BoundingBox(19.0, 47.0, 19.0, 47.0), BoundingBox.parse("19,47,19,47"))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "", "1,2,3", "1,2,3,4,5", "a,2,3,4", "1,,3,4",
            "19.3,47.3,18.9,47.7", "18.9,47.7,19.3,47.3",
            "18.9,-91,19.3,47", "18.9,47,19.3,91", "-181,47,19,48", "18,47,181,48",
        ],
    )
    fun `rejects malformed and impossible boxes`(value: String) {
        assertThrows<InvalidRequestException> { BoundingBox.parse(value) }
    }
}
