package com.partymap.backend.domain.common.geo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class HungaryTest {
    @Test
    fun `loads the closed outline from the resources`() {
        assertTrue(Hungary.outline.size > 1000)
        assertEquals(Hungary.outline.first(), Hungary.outline.last())
        assertEquals(16.1139 to 46.8691, Hungary.outline.first())
    }

    @ParameterizedTest(name = "{0} is inside")
    @CsvSource(
        "Budapest, 47.4979, 19.0402",
        "Szeged, 46.2530, 20.1414",
        "Sopron, 47.6817, 16.5845",
        "Záhony, 48.4070, 22.1780",
        "Balatonfűzfő, 47.0538, 18.0599",
        "Mohács, 45.9930, 18.6830",
    )
    fun `a place in the country is inside`(name: String, latitude: Double, longitude: Double) {
        assertTrue(Hungary.contains(latitude, longitude), name)
    }

    @ParameterizedTest(name = "{0} is outside")
    @CsvSource(
        "Kraków, 50.0678, 19.9362",
        "Vienna, 48.2082, 16.3738",
        "Bratislava, 48.1486, 17.1077",
        "Komárno (across the Danube from Komárom), 47.7630, 18.1280",
        "Subotica, 46.1000, 19.6650",
        "Oradea, 47.0465, 21.9189",
        "Null island, 0.0, 0.0",
    )
    fun `a place beyond the border is outside`(name: String, latitude: Double, longitude: Double) {
        assertFalse(Hungary.contains(latitude, longitude), name)
    }
}
