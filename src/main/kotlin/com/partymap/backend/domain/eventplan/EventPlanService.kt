package com.partymap.backend.domain.eventplan

import com.partymap.backend.domain.event.db.EventEntity
import com.partymap.backend.domain.event.db.EventLineupItemEntity
import com.partymap.backend.domain.event.db.EventLineupItemId
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.eventplan.db.*
import com.partymap.backend.domain.eventplan.exception.AlreadyInvitedPerformerException
import com.partymap.backend.domain.eventplan.exception.InvalidStartOrEndTimeException
import com.partymap.backend.domain.eventplan.exception.NoValidPlaceInvitationException
import com.partymap.backend.domain.eventplan.exception.PendingLineupInvitationException
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.user.UserEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EventPlanService(
    private val eventPlanRepository: EventPlanRepository,
    private val eventPlanPlaceInvitationRepository: EventPlanPlaceInvitationRepository,
    private val eventRepository: EventRepository,
) {

    @Transactional
    @Throws(
        AlreadyInvitedPerformerException::class,
        InvalidStartOrEndTimeException::class,
    )
    fun invitePerformer(
        eventPlan: EventPlanEntity,
        performer: PerformerEntity,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ) {
        // Check if performer was already invited
        if (eventPlan.lineupInvitations.any { it.id.performer == performer }) {
            throw AlreadyInvitedPerformerException()
        }

        if (endTime.isBefore(startTime)) {
            throw InvalidStartOrEndTimeException()
        }

        eventPlan.lineupInvitations.add(
            EventPlanLineupInvitationEntity(
                id = EventPlanLineupItemId(eventPlan, performer),
                startTime = startTime,
                endTime = endTime,
            ),
        )
    }

    @Transactional
    fun invitePlace(eventPlan: EventPlanEntity, place: PlaceEntity) {
        // Delete invitation if that already exists
        if (eventPlan.placeInvitations.size == 1) {
            val existingInvitation = eventPlan.placeInvitations[0]
            eventPlanPlaceInvitationRepository.delete(existingInvitation)
            eventPlan.placeInvitations.remove(existingInvitation)
        }

        eventPlan.placeInvitations.add(
            EventPlanPlaceInvitationEntity(
                id = EventPlanPlaceInvitationEntityId(eventPlan, place),
            ),
        )
    }

    @Transactional
    @Throws(
        NoValidPlaceInvitationException::class,
        PendingLineupInvitationException::class,
    )
    fun publish(user: UserEntity, eventPlan: EventPlanEntity) {
        // Check if there is a place who accepted the invitation
        if (!eventPlan.placeInvitations.any { it.state == EventPlanPlaceInvitationState.ACCEPTED }) {
            throw NoValidPlaceInvitationException()
        }

        // Check if there are no pending performer invitations
        if (eventPlan.lineupInvitations.any { it.state == EventPlanLineupInvitationState.PENDING }) {
            throw PendingLineupInvitationException()
        }

        val event = eventRepository.save(
            EventEntity(
                title = eventPlan.title,
                description = eventPlan.description,
                start = eventPlan.startDateTime,
                end = eventPlan.endDateTime,
                image = eventPlan.image,
                price = eventPlan.price,
                kind = eventPlan.kind,
                links = eventPlan.links,
                place = eventPlan.placeInvitations.first().id.place,
                owner = user,
            ),
        )

        event.lineupItems = eventPlan.lineupInvitations.filter { it.state == EventPlanLineupInvitationState.ACCEPTED }
            .map {
                EventLineupItemEntity(
                    id = EventLineupItemId(
                        event = event,
                        performer = it.id.performer,
                    ),
                    startTime = it.startTime,
                    endTime = it.endTime,
                )
            }.toMutableList()

        eventPlanRepository.delete(eventPlan)
    }

    @Transactional
    fun respondToPerformerInvitation(
        eventPlan: EventPlanEntity,
        performer: PerformerEntity,
        newState: EventPlanLineupInvitationState,
    ) {
        val invitation = eventPlan.lineupInvitations.firstOrNull { it.id.performer.id == performer.id }
            ?: throw IllegalArgumentException(
                "Lineup invitation for performer ${performer.id} not found in event plan ${eventPlan.id}",
            )

        invitation.state = newState

        eventPlanRepository.save(eventPlan)
    }
}
