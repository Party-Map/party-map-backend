package com.partymap.backend.web.shell

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDateTime
import java.time.ZoneId

class InlineJsonTest {
    @Test
    fun `escapes everything that could end the script or start a tag`() {
        assertEquals(
            "{\"t\":\"\\u003c/script\\u003e \\u0026 \\u2028\\u2029\"}",
            InlineJson.safe("{\"t\":\"</script> & \u2028\u2029\"}"),
        )
    }
}

class ShellMarkersTest {
    private val template = "<html><head><!--pm:head--><title>x</title><!--/pm:head--></head>" +
        "<body><div id=\"root\"><!--pm:body--><!--/pm:body--></div><!--pm:data--><!--/pm:data--></body></html>"

    @Test
    fun `replaces the three regions, markers included`() {
        assertEquals(
            "<html><head><title>y</title></head>" +
                "<body><div id=\"root\"><h1>b</h1></div><script>d</script></body></html>",
            ShellMarkers.splice(template, "<title>y</title>", "<h1>b</h1>", "<script>d</script>"),
        )
    }

    @Test
    fun `falls back to the anchors of a template without markers`() {
        val bare = "<html><HEAD></HEAD><body><div id=\"root\"></div></body></html>"
        assertEquals(
            "<html><HEAD>H</HEAD><body><div id=\"root\">B</div>D</body></html>",
            ShellMarkers.splice(bare, "H", "B", "D"),
        )
    }

    @Test
    fun `appends when there is nothing to anchor to`() {
        assertEquals("<p/>HBD", ShellMarkers.splice("<p/>", "H", "B", "D"))
    }
}

class SeoTextTest {
    @Test
    fun `collapses whitespace, falls back when blank and cuts long text at a word`() {
        assertEquals("An  evening".replace("  ", " "), SeoText.description("An \n evening ", "x"))
        assertEquals("Fallback.", SeoText.description("  ", "Fallback."))
        val long = ("word ".repeat(50)).trim()
        val cut = SeoText.description(long, "x")
        assertEquals(true, cut.length <= SeoText.MAX_DESCRIPTION)
        assertEquals(true, cut.endsWith("word…"), cut)
    }

    @Test
    fun `formats titles and times`() {
        assertEquals("Pond Party · 1 Jun at Akvárium | PartyMap", SeoText.title("Pond Party", "1 Jun at Akvárium"))
        assertEquals("Not found | PartyMap", SeoText.title("Not found", ""))
        val budapest = ZoneId.of("Europe/Budapest")
        assertEquals("2030-06-01T20:00:00+02:00", SeoText.offset(LocalDateTime.of(2030, 6, 1, 20, 0), budapest))
        assertEquals("2030-01-01T20:00:00+01:00", SeoText.offset(LocalDateTime.of(2030, 1, 1, 20, 0), budapest))
        assertEquals("1 Jun", SeoText.dayMonth(LocalDateTime.of(2030, 6, 1, 20, 0)))
        assertEquals(
            "1 June 2030, 20:00 – 2 June 2030, 04:00",
            SeoText.range(LocalDateTime.of(2030, 6, 1, 20, 0), LocalDateTime.of(2030, 6, 2, 4, 0)),
        )
    }
}

class JsonLdPriceTest {
    @ParameterizedTest
    @CsvSource(
        "Free, 0, HUF",
        "FREE entry, 0, HUF",
        "3000 HUF, 3000, HUF",
        "'4,500 Ft', 4500, HUF",
        "€15, 15, EUR",
        "15 eur, 15, EUR",
        "$12.50, 12.50, USD",
        "£8, 8, GBP",
    )
    fun `parses the amount and the currency`(raw: String, amount: String, currency: String) {
        assertEquals(amount to currency, JsonLd.parsePrice(raw))
    }

    @Test
    fun `gives up on prices without a number`() {
        assertNull(JsonLd.parsePrice(null))
        assertNull(JsonLd.parsePrice("  "))
        assertNull(JsonLd.parsePrice("ask at the door"))
        assertNull(JsonLd.offer("ask at the door", "u"))
        assertEquals("https://schema.org/InStock", JsonLd.offer("Free", "u")?.get("availability"))
    }
}
