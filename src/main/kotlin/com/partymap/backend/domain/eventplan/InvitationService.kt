package com.partymap.backend.domain.eventplan

import com.partymap.backend.domain.common.exception.NotFoundException
import com.partymap.backend.domain.common.exception.requireOwner
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationRepository
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationRepository
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationState
import com.partymap.backend.domain.eventplan.db.InvitationAnswer
import com.partymap.backend.domain.eventplan.dto.EventPlanLineupInvitationForPerformerDto
import com.partymap.backend.domain.eventplan.dto.EventPlanPlaceInvitationWithDateDto
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.place.db.PlaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/** The managers' side of the workflow: the invitations their place or performer received, and their answers. */
@Service
@Transactional
class InvitationService(
    private val placeRepository: PlaceRepository,
    private val performerRepository: PerformerRepository,
    private val placeInvitationRepository: EventPlanPlaceInvitationRepository,
    private val lineupInvitationRepository: EventPlanLineupInvitationRepository,
) {
    @Transactional(readOnly = true)
    fun placeInvitations(sub: UUID, placeId: UUID): List<EventPlanPlaceInvitationWithDateDto> {
        requirePlaceOwner(sub, placeId, "see the invitations of this place")
        return placeInvitationRepository.findAllByPlaceId(placeId).map { it.toWithDateDto() }
    }

    fun respondAsPlace(sub: UUID, placeId: UUID, eventPlanId: UUID, answer: InvitationAnswer) {
        requirePlaceOwner(sub, placeId, "answer invitations for this place")
        val invitation = placeInvitationRepository.findByPlaceIdAndEventPlanId(placeId, eventPlanId)
            .orElseThrow { NotFoundException("Event plan $eventPlanId has not invited place $placeId.") }
        invitation.state = when (answer) {
            InvitationAnswer.ACCEPT -> EventPlanPlaceInvitationState.ACCEPTED
            InvitationAnswer.REJECT -> EventPlanPlaceInvitationState.REJECTED
        }
    }

    @Transactional(readOnly = true)
    fun performerInvitations(sub: UUID, performerId: UUID): List<EventPlanLineupInvitationForPerformerDto> {
        requirePerformerOwner(sub, performerId, "see the invitations of this performer")
        return lineupInvitationRepository.findAllByPerformerId(performerId).map { it.toForPerformerDto() }
    }

    fun respondAsPerformer(sub: UUID, performerId: UUID, eventPlanId: UUID, answer: InvitationAnswer) {
        requirePerformerOwner(sub, performerId, "answer invitations for this performer")
        val invitation = lineupInvitationRepository.findByPerformerIdAndEventPlanId(performerId, eventPlanId)
            .orElseThrow { NotFoundException("Event plan $eventPlanId has not invited performer $performerId.") }
        invitation.state = when (answer) {
            InvitationAnswer.ACCEPT -> EventPlanLineupInvitationState.ACCEPTED
            InvitationAnswer.REJECT -> EventPlanLineupInvitationState.REJECTED
        }
    }

    private fun requirePlaceOwner(sub: UUID, placeId: UUID, action: String) {
        val place = placeRepository.findById(placeId).orElseThrow { NotFoundException("Place", placeId) }
        requireOwner(place.owner.sub, sub, action)
    }

    private fun requirePerformerOwner(sub: UUID, performerId: UUID, action: String) {
        val performer = performerRepository.findById(
            performerId,
        ).orElseThrow { NotFoundException("Performer", performerId) }
        requireOwner(performer.owner.sub, sub, action)
    }
}
