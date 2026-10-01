package com.partymap.backend.web.shell

import org.springframework.stereotype.Component
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import tools.jackson.databind.json.JsonMapper
import java.util.Locale

/** Renders a [ShellPage]'s head and body with Thymeleaf and splices them, with the data, into the SPA's template. */
@Component
class ShellRenderer(
    private val templateEngine: ITemplateEngine,
    private val jsonMapper: JsonMapper,
    private val templateSource: ShellTemplateSource,
) {
    fun render(page: ShellPage): String {
        val headModel = mapOf("meta" to page.meta, "jsonLd" to jsonLd(page))
        val head = templateEngine.process("shell/head", context(headModel))
        val body = templateEngine.process("shell/${page.view}", context(page.model))
        val data = page.data?.let { payload ->
            """<script type="application/json" id="pm-data">${InlineJson.safe(
                jsonMapper.writeValueAsString(payload),
            )}</script>"""
        }.orEmpty()
        return ShellMarkers.splice(templateSource.template(), head, body, data)
    }

    private fun jsonLd(page: ShellPage): String? = page.jsonLd?.let {
        InlineJson.safe(
            jsonMapper.writeValueAsString(it),
        )
    }

    private fun context(variables: Map<String, Any?>): Context = Context(Locale.ENGLISH).apply {
        variables.forEach { (name, value) -> setVariable(name, value) }
    }
}
