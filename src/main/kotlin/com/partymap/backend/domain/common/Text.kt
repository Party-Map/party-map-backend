package com.partymap.backend.domain.common

/** The trimmed text, or null when nothing but whitespace is left (optional fields sent as ""). */
fun String?.trimToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
