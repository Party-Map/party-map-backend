package com.partymap.backend.domain.eventplan

import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.common.dto.LinkDto
import com.partymap.backend.domain.eventplan.db.EventPlanEntity
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationEntity
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationEntity
import com.partymap.backend.domain.eventplan.dto.*
import com.partymap.backend.domain.performer.toDto
import com.partymap.backend.domain.place.toDto
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
        placeInvitation = placeInvitations.map { it.toDto() }.getOrNull(0),
        lineupInvitations = lineupInvitations.map { it.toDto() },
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
    title = dto.title
    kind = dto.kind
    description = dto.description
    image = dto.image
    startDateTime = dto.startDateTime
    endDateTime = dto.endDateTime
    price = dto.price
    links = (dto.links ?: emptyList()).map {
        LinkEmbeddable(
            type = it.type,
            url = it.url,
        )
    }.toMutableList()
}

fun EventPlanPlaceInvitationEntity.toDto(): EventPlanPlaceInvitationDto =
    EventPlanPlaceInvitationDto(
        state = state,
        place = id.place.toDto()
    )

fun EventPlanLineupInvitationEntity.toDto(): EventPlanLineupInvitationDto =
    EventPlanLineupInvitationDto(
        state = state,
        performer = id.performer.toDto(),
        startTime = startTime,
        endTime = endTime,
    )

fun EventPlanLineupInvitationEntity.toForPerformerDto(): EventPlanLineupInvitationForPerformerDto =
    EventPlanLineupInvitationForPerformerDto(
        eventPlanId = id.eventPlan.id!!,
        eventPlanTitle = id.eventPlan.title,
        state = state,
        performer = id.performer.toDto(),
        startTime = startTime,
        endTime = endTime,
    )

