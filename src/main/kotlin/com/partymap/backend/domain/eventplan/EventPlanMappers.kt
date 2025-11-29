package com.partymap.backend.domain.eventplan

import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.eventplan.db.EventPlanAdminListItemDto
import com.partymap.backend.domain.eventplan.db.EventPlanCreateDto
import com.partymap.backend.domain.eventplan.db.EventPlanDto
import com.partymap.backend.domain.eventplan.db.EventPlanEntity
import com.partymap.backend.domain.like.dto.LinkDto
import com.partymap.backend.domain.user.UserEntity

fun EventPlanEntity.toDto(): EventPlanDto =
    EventPlanDto(
        id = id!!,
        title = title,
        description = description,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        image = image,
        price = price,
        kind = kind,
        links = links.map { LinkDto(it.type, it.url) },
    )

fun EventPlanEntity.toAdminListItemDto(): EventPlanAdminListItemDto =
    EventPlanAdminListItemDto(
        id = id!!,
        title = title,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
    )

fun EventPlanCreateDto.toEntity(owner: UserEntity): EventPlanEntity =
    EventPlanEntity(
        title = title,
        kind = kind,
        description = description,
        image = image,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        price = price,
        links = (links ?: emptyList()).map {
            LinkEmbeddable(
                type = it.type,
                url = it.url,
            )
        }.toMutableList(),
        owner = owner,
    )

fun EventPlanEntity.updateFromDto(dto: EventPlanCreateDto) {
    title = title
    kind = kind
    description = description
    image = image
    startDateTime = startDateTime
    endDateTime = endDateTime
    price = price
    links = (links ?: emptyList()).map {
        LinkEmbeddable(
            type = it.type,
            url = it.url,
        )
    }.toMutableList()
    owner = owner
}