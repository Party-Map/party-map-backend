package com.partymap.backend.web.shell

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Clock

@Configuration
@EnableConfigurationProperties(ShellProperties::class)
class ShellConfig {
    /** Fetches the SPA's template over HTTP; a classpath page without the app stands in until the first fetch works. */
    @Bean
    fun shellTemplateSource(properties: ShellProperties, clock: Clock): ShellTemplateSource {
        // HTTP/1.1 only: the JDK client's default h2c upgrade request is never answered by the frontend's dev
        // server (it treats Upgrade headers as websocket handshakes), which would stall every fetch.
        val http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(properties.connectTimeout)
            .build()
        val requestFactory = JdkClientHttpRequestFactory(http).apply { setReadTimeout(properties.readTimeout) }
        val client = RestClient.builder().requestFactory(requestFactory).build()
        val fallback = ClassPathResource("shell/fallback-index.html").getContentAsString(Charsets.UTF_8)
        return HttpShellTemplateSource(client, properties, clock, fallback)
    }
}
