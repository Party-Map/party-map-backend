package com.partymap.backend.domain.like.dto

import com.partymap.backend.domain.common.db.LinkType

data class LinkDto(
    val type: LinkType,
    val url: String,
)
