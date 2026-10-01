package com.partymap.backend.web.shell

/** JSON that is safe inside a `<script>` element: nothing in it can close the element or start a tag. */
object InlineJson {
    fun safe(json: String): String = json
        .replace("<", "\\u003c")
        .replace(">", "\\u003e")
        .replace("&", "\\u0026")
        .replace("\u2028", "\\u2028")
        .replace("\u2029", "\\u2029")
}
