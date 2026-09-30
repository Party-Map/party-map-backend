package com.partymap.backend.domain.eventplan.db

import com.partymap.backend.domain.common.exception.InvalidRequestException

/** A manager's answer to an invitation, as sent in `?state=accept|reject` (case does not matter). */
enum class InvitationAnswer {
    ACCEPT,
    REJECT,
    ;

    companion object {
        fun parse(value: String): InvitationAnswer = entries.firstOrNull {
            it.name.equals(
                value.trim(),
                ignoreCase = true,
            )
        }
            ?: throw InvalidRequestException("Unknown answer '$value'; use accept or reject.")
    }
}
