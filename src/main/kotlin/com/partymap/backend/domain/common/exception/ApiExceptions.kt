package com.partymap.backend.domain.common.exception

import org.springframework.http.HttpStatus
import java.util.UUID

/** A failure the client caused or can act on; `RestExceptionHandler` turns it into an RFC 9457 problem response. */
abstract class ApiException(val status: HttpStatus, override val message: String) : RuntimeException(message)

class NotFoundException(message: String) : ApiException(HttpStatus.NOT_FOUND, message) {
    constructor(what: String, id: UUID) : this("$what $id was not found.")
}

class ForbiddenException(message: String) : ApiException(HttpStatus.FORBIDDEN, message)

class UnauthorizedException(message: String) : ApiException(HttpStatus.UNAUTHORIZED, message)

/** The request is well-formed but breaks a business rule (bad times, unknown answer...). */
open class InvalidRequestException(message: String) : ApiException(HttpStatus.BAD_REQUEST, message)

/** The request conflicts with the current state (already invited, not publishable yet...). */
open class ConflictException(message: String) : ApiException(HttpStatus.CONFLICT, message)

/** Throws [ForbiddenException] unless [ownerSub] is the caller; [action] completes "You are not allowed to ...". */
fun requireOwner(ownerSub: UUID, callerSub: UUID, action: String) {
    if (ownerSub != callerSub) throw ForbiddenException("You are not allowed to $action.")
}

/** A service the API depends on (Keycloak) failed or is misconfigured; the message never carries its answer. */
class UpstreamException(message: String, cause: Throwable? = null) : ApiException(HttpStatus.BAD_GATEWAY, message) {
    init {
        cause?.let(::initCause)
    }
}
