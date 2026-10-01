package com.partymap.backend.web.shell

/** The `<head>` of a shell. [canonical] is null for pages that must not be indexed. */
data class ShellMeta(
    val title: String,
    val description: String,
    val canonical: String?,
    val image: String?,
    val noindex: Boolean = false,
)

/**
 * One rendered page: its metadata, the body template under `templates/shell/` with its model, the schema.org
 * graph, and the data the app seeds its cache from (`null` where nothing is preloaded).
 */
data class ShellPage(
    val meta: ShellMeta,
    val view: String,
    val model: Map<String, Any?>,
    val jsonLd: Map<String, Any?>? = null,
    val data: Map<String, Any?>? = null,
)
