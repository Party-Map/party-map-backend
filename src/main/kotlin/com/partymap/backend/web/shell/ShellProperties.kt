package com.partymap.backend.web.shell

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration
import java.time.ZoneId

/** The server-rendered shells (`app.shell.*`): where the SPA's template comes from and how pages are addressed. */
@ConfigurationProperties("app.shell")
data class ShellProperties(
    /** The frontend's `index.html`, fetched and cached for [templateTtl]. */
    val templateUrl: String,
    /** The site as the public sees it, for canonical URLs and JSON-LD, e.g. `https://terkep.party`. */
    val publicBaseUrl: String,
    /** The zone the zone-less event times are shown in. */
    val zone: String = "Europe/Budapest",
    val templateTtl: Duration = DEFAULT_TEMPLATE_TTL,
    val connectTimeout: Duration = DEFAULT_CONNECT_TIMEOUT,
    val readTimeout: Duration = DEFAULT_READ_TIMEOUT,
) {
    val zoneId: ZoneId get() = ZoneId.of(zone)
    val baseUrl: String get() = publicBaseUrl.trimEnd('/')
}

private val DEFAULT_TEMPLATE_TTL = Duration.ofSeconds(60)
private val DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(2)
private val DEFAULT_READ_TIMEOUT = Duration.ofSeconds(3)
