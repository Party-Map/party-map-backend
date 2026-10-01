package com.partymap.backend.web.shell

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * The three regions of the SPA's `index.html` a shell replaces: `<!--pm:head-->…<!--/pm:head-->` in the head,
 * `<!--pm:body-->…<!--/pm:body-->` inside `#root`, `<!--pm:data-->…<!--/pm:data-->` before `</body>`. A template
 * without a marker still gets the content, at the matching anchor, and a warning once.
 */
object ShellMarkers {
    const val HEAD = "pm:head"
    const val BODY = "pm:body"
    const val DATA = "pm:data"

    private val log = LoggerFactory.getLogger(ShellMarkers::class.java)!!
    private val warned = ConcurrentHashMap.newKeySet<String>()

    fun splice(template: String, head: String, body: String, data: String): String {
        var html = replaceRegion(template, HEAD, head) { insertBefore(it, "</head>", head) }
        html = replaceRegion(html, BODY, body) { insertAfter(it, "<div id=\"root\">", body) }
        return replaceRegion(html, DATA, data) { insertBefore(it, "</body>", data) }
    }

    private fun replaceRegion(html: String, name: String, content: String, fallback: (String) -> String): String {
        val endMarker = "<!--/$name-->"
        val start = html.indexOf("<!--$name-->")
        val end = html.indexOf(endMarker)
        if (start < 0 || end < start) {
            if (warned.add(name)) log.warn("The shell template has no <!--{}--> region; inserting at its anchor", name)
            return fallback(html)
        }
        return html.substring(0, start) + content + html.substring(end + endMarker.length)
    }

    private fun insertBefore(html: String, anchor: String, content: String): String {
        val at = html.indexOf(anchor, ignoreCase = true)
        return if (at < 0) html + content else html.substring(0, at) + content + html.substring(at)
    }

    private fun insertAfter(html: String, anchor: String, content: String): String {
        val at = html.indexOf(anchor, ignoreCase = true)
        if (at < 0) return html + content
        val after = at + anchor.length
        return html.substring(0, after) + content + html.substring(after)
    }
}
