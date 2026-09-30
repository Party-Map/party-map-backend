package com.partymap.backend.domain.eventplan

import com.partymap.backend.domain.common.dto.toDto
import com.partymap.backend.domain.common.dto.toEmbeddables
import com.partymap.backend.domain.common.trimToNull
import com.partymap.backend.domain.eventplan.db.EventPlanEntity
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationEntity
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationEntity
import com.partymap.backend.domain.eventplan.dto.EventPlanAdminListItemDto
import com.partymap.backend.domain.eventplan.dto.EventPlanCreateDto
import com.partymap.backend.domain.eventplan.dto.EventPlanDto
import com.partymap.backend.domain.eventplan.dto.EventPlanLineupInvitationDto
import com.partymap.backend.domain.eventplan.dto.EventPlanLineupInvitationForPerformerDto
import com.partymap.backend.domain.eventplan.dto.EventPlanPlaceInvitationDto
import com.partymap.backend.domain.eventplan.dto.EventPlanPlaceInvitationWithDateDto
import com.partymap.backend.domain.performer.toDto
import com.partymap.backend.domain.place.toDto
import com.partymap.backend.domain.user.UserEntity

fun EventPlanEntity.toDto(): EventPlanDto = EventPlanDto(
    id = requiredId,
    title = title,
    description = description,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
    image = image,
    price = price,
    kind = kind,
    links = links.map { it.toDto() },
    placeInvitation = placeInvitation?.toDto(),
    lineupInvitations = lineupInvitations.map { it.toDto() },
)

fun EventPlanEntity.toAdminListItemDto(): EventPlanAdminListItemDto = EventPlanAdminListItemDto(
    id = requiredId,
    title = title,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
)

fun EventPlanCreateDto.toEntity(owner: UserEntity): EventPlanEntity = EventPlanEntity(
    title = title.trim(),
    description = description,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
    image = image.trimToNull(),
    price = price.trimToNull(),
    kind = kind,
    links = links.toEmbeddables(),
    owner = owner,
)

fun EventPlanEntity.updateFromDto(dto: EventPlanCreateDto) {
    title = dto.title.trim()
    description = dto.description
    startDateTime = dto.startDateTime
    endDateTime = dto.endDateTime
    image = dto.image.trimToNull()
    price = dto.price.trimToNull()
    kind = dto.kind
    links.clear()
    links.addAll(dto.links.toEmbeddables())
}

fun EventPlanPlaceInvitationEntity.toDto(): EventPlanPlaceInvitationDto =
    EventPlanPlaceInvitationDto(state = state, place = id.place.toDto())

fun EventPlanPlaceInvitationEntity.toWithDateDto(): EventPlanPlaceInvitationWithDateDto =
    EventPlanPlaceInvitationWithDateDto(
        eventPlanId = id.eventPlan.requiredId,
        state = state,
        title = id.eventPlan.title,
        startDateTime = id.eventPlan.startDateTime,
        endDateTime = id.eventPlan.endDateTime,
    )

fun EventPlanLineupInvitationEntity.toDto(): EventPlanLineupInvitationDto = EventPlanLineupInvitationDto(
    state = state,
    startTime = startTime,
    endTime = endTime,
    performer = id.performer.toDto(),
)

fun EventPlanLineupInvitationEntity.toForPerformerDto(): EventPlanLineupInvitationForPerformerDto =
    EventPlanLineupInvitationForPerformerDto(
        eventPlanId = id.eventPlan.requiredId,
        eventPlanTitle = id.eventPlan.title,
        state = state,
        startTime = startTime,
        endTime = endTime,
        performer = id.performer.toDto(),
    )
