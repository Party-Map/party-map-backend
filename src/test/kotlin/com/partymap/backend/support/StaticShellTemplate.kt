package com.partymap.backend.support

import com.partymap.backend.web.shell.ShellTemplateSource
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Primary

/** A tiny SPA template with the three marker regions, so the shell tests never fetch the frontend. */
@TestComponent
@Primary
class StaticShellTemplate : ShellTemplateSource {
    override fun template(): String = TEMPLATE

    companion object {
        const val TEMPLATE = "<!doctype html><html><head><meta charset=\"UTF-8\" />" +
            "<!--pm:head--><title>PartyMap</title><!--/pm:head--></head>" +
            "<body><div id=\"root\"><!--pm:body--><!--/pm:body--></div>" +
            "<!--pm:data--><!--/pm:data--><script src=\"/static/js/index.js\"></script></body></html>"
    }
}
