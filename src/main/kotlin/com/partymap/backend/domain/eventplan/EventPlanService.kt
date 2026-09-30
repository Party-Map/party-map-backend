package com.partymap.backend.domain.eventplan

import com.partymap.backend.domain.common.db.LinkEmbeddable
import com.partymap.backend.domain.common.exception.NotFoundException
import com.partymap.backend.domain.common.exception.requireOwner
import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventLineupItemEntity
import com.partymap.backend.domain.event.db.EventLineupItemId
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.eventplan.db.EventPlanEntity
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationEntity
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.eventplan.db.EventPlanLineupItemId
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationEntity
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationEntityId
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationState
import com.partymap.backend.domain.eventplan.db.EventPlanRepository
import com.partymap.backend.domain.eventplan.dto.EventPlanAdminListItemDto
import com.partymap.backend.domain.eventplan.dto.EventPlanCreateDto
import com.partymap.backend.domain.eventplan.dto.EventPlanDto
import com.partymap.backend.domain.eventplan.dto.EventPlanLineupInvitationCreatePayloadDto
import com.partymap.backend.domain.eventplan.dto.EventPlanLineupInvitationDto
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/** The organizer's side of the event plan workflow; every method checks that [UUID] `sub` owns the plan. */
@Service
@Transactional
class EventPlanService(
    private val eventPlanRepository: EventPlanRepository,
    private val placeRepository: PlaceRepository,
    private val performerRepository: PerformerRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun get(sub: UUID, id: UUID): EventPlanDto = ownedPlan(sub, id, "view this event plan").toDto()

    @Transactional(readOnly = true)
    fun owned(sub: UUID): List<EventPlanAdminListItemDto> =
        eventPlanRepository.findAllByOwnerSubOrderByStartDateTimeAsc(sub).map { it.toAdminListItemDto() }

    fun create(sub: UUID, dto: EventPlanCreateDto): EventPlanDto {
        requireTimeRange(dto.startDateTime, dto.endDateTime, "event")
        return eventPlanRepository.save(dto.toEntity(userRepository.getReferenceById(sub))).toDto()
    }

    fun update(sub: UUID, id: UUID, dto: EventPlanCreateDto): EventPlanDto {
        val plan = ownedPlan(sub, id, "edit this event plan")
        requireTimeRange(dto.startDateTime, dto.endDateTime, "event")
        plan.updateFromDto(dto)
        return eventPlanRepository.saveAndFlush(plan).toDto()
    }

    /** Invites [placeId], replacing an invitation to another place; inviting the same place again resets its answer. */
    fun invitePlace(sub: UUID, id: UUID, placeId: UUID) {
        val plan = ownedPlan(sub, id, "edit this event plan")
        val place = placeRepository.findById(placeId).orElseThrow { NotFoundException("Place", placeId) }
        val current = plan.placeInvitation
        if (current != null && current.id.place.id == placeId) {
            current.state = EventPlanPlaceInvitationState.PENDING
            return
        }
        plan.placeInvitations.clear()
        plan.placeInvitations.add(EventPlanPlaceInvitationEntity(EventPlanPlaceInvitationEntityId(plan, place)))
    }

    @Transactional(readOnly = true)
    fun lineupInvitations(sub: UUID, id: UUID): List<EventPlanLineupInvitationDto> =
        ownedPlan(sub, id, "view this event plan").lineupInvitations.map { it.toDto() }

    fun invitePerformer(sub: UUID, id: UUID, slot: EventPlanLineupInvitationCreatePayloadDto) {
        val plan = ownedPlan(sub, id, "edit this event plan")
        requireTimeRange(slot.startTime, slot.endTime, "lineup slot")
        if (slot.startTime.isBefore(plan.startDateTime) || slot.endTime.isAfter(plan.endDateTime)) {
            throw LineupSlotOutsidePlanException()
        }
        val performer = performerRepository.findById(slot.performerId)
            .orElseThrow { NotFoundException("Performer", slot.performerId) }
        if (plan.lineupInvitations.any { it.id.performer.id == performer.id }) throw AlreadyInvitedPerformerException()
        plan.lineupInvitations.add(
            EventPlanLineupInvitationEntity(
                id = EventPlanLineupItemId(plan, performer),
                startTime = slot.startTime,
                endTime = slot.endTime,
            ),
        )
    }

    fun removePerformer(sub: UUID, id: UUID, performerId: UUID) {
        val plan = ownedPlan(sub, id, "edit this event plan")
        if (!plan.lineupInvitations.removeIf { it.id.performer.id == performerId }) {
            throw NotFoundException("Performer $performerId is not in the lineup of event plan $id.")
        }
    }

    /**
     * Turns the plan into an event at the accepted place with the accepted performers, then deletes the plan and its
     * invitations. Rejected performers are left out; a pending one blocks publishing.
     */
    fun publish(sub: UUID, id: UUID): UUID {
        val plan = ownedPlan(sub, id, "publish this event plan")
        val placeInvitation = plan.placeInvitation?.takeIf { it.state == EventPlanPlaceInvitationState.ACCEPTED }
            ?: throw NoAcceptedPlaceException()
        if (plan.lineupInvitations.any { it.state == EventPlanLineupInvitationState.PENDING }) {
            throw PendingLineupInvitationException()
        }
        val event = eventRepository.save(
            EventEntity(
                title = plan.title,
                place = placeInvitation.id.place,
                description = plan.description,
                start = plan.startDateTime,
                end = plan.endDateTime,
                image = plan.image,
                price = plan.price,
                kind = plan.kind,
                links = plan.links.map { LinkEmbeddable(it.type, it.url) }.toMutableList(),
                owner = plan.owner,
            ),
        )
        plan.lineupInvitations
            .filter { it.state == EventPlanLineupInvitationState.ACCEPTED }
            .forEach {
                event.lineupItems.add(
                    EventLineupItemEntity(EventLineupItemId(event, it.id.performer), it.startTime, it.endTime),
                )
            }
        eventPlanRepository.delete(plan)
        return event.requiredId
    }

    private fun ownedPlan(sub: UUID, id: UUID, action: String): EventPlanEntity {
        val plan = eventPlanRepository.findById(id).orElseThrow { NotFoundException("Event plan", id) }
        requireOwner(plan.owner.sub, sub, action)
        return plan
    }

    private fun requireTimeRange(start: LocalDateTime, end: LocalDateTime, what: String) {
        if (!end.isAfter(start)) throw InvalidTimeRangeException(what)
    }
}
