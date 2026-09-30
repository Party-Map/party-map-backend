package com.partymap.backend.domain.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TextTest {
    @Test
    fun `trims text and turns blank or missing text into null`() {
        assertEquals("Akvárium", "  Akvárium ".trimToNull())
        assertNull("   ".trimToNull())
        assertNull("".trimToNull())
        assertNull(null.trimToNull())
    }
}
