package com.partymap.backend.web.shell

import com.partymap.backend.support.MutableClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withException
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.io.IOException
import java.time.Duration

class HttpShellTemplateSourceTest {
    private val url = "http://frontend.test/index.html"
    private val clock = MutableClock()
    private val builder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val source = HttpShellTemplateSource(
        builder.build(),
        ShellProperties(
            templateUrl = url,
            publicBaseUrl = "http://localhost:3000",
            templateTtl = Duration.ofSeconds(60),
        ),
        clock,
        fallback = "<fallback/>",
    )

    private fun expect(html: String) {
        server.expect(requestTo(url)).andRespond(withSuccess(html, MediaType.TEXT_HTML))
    }

    @Test
    fun `fetches once within the TTL and again after it`() {
        expect("<v1/>")
        expect("<v2/>")

        assertEquals("<v1/>", source.template())
        clock.advance(Duration.ofSeconds(59))
        assertEquals("<v1/>", source.template())
        clock.advance(Duration.ofSeconds(2))
        assertEquals("<v2/>", source.template())
        server.verify()
    }

    @Test
    fun `keeps the last good copy when a refresh fails and retries after another TTL`() {
        expect("<v1/>")
        server.expect(requestTo(url)).andRespond(withStatus(HttpStatus.BAD_GATEWAY))
        expect("<v3/>")

        assertEquals("<v1/>", source.template())
        clock.advance(Duration.ofSeconds(61))
        assertEquals("<v1/>", source.template())
        assertEquals("<v1/>", source.template(), "the failure is not retried before the next TTL")
        clock.advance(Duration.ofSeconds(61))
        assertEquals("<v3/>", source.template())
        server.verify()
    }

    @Test
    fun `stands in with the fallback page until the first fetch works, also for an empty answer`() {
        server.expect(requestTo(url)).andRespond(withException(IOException("down")))
        expect("   ")
        expect("<v1/>")

        assertEquals("<fallback/>", source.template())
        clock.advance(Duration.ofSeconds(61))
        assertEquals("<fallback/>", source.template())
        clock.advance(Duration.ofSeconds(61))
        assertEquals("<v1/>", source.template())
        server.verify()
    }
}
