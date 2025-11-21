package com.partymap.backend.api.dtos

import com.partymap.backend.domain.common.db.LinkType

data class LinkDto(
    val type: LinkType,
    val url: String,
)
