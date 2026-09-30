package com.partymap.backend.domain.eventplan

import com.partymap.backend.domain.common.exception.ConflictException
import com.partymap.backend.domain.common.exception.InvalidRequestException

class InvalidTimeRangeException(what: String) : InvalidRequestException("The $what must end after it starts.")

class LineupSlotOutsidePlanException :
    InvalidRequestException(
        "A lineup slot must fit between the plan's start and end.",
    )

class AlreadyInvitedPerformerException : ConflictException("This performer is already in the lineup.")

class NoAcceptedPlaceException :
    ConflictException(
        "The plan can be published once a place has accepted the invitation.",
    )

class PendingLineupInvitationException :
    ConflictException("The plan can be published once every invited performer has answered.")
