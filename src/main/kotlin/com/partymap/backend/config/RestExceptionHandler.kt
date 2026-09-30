package com.partymap.backend.config

import com.partymap.backend.domain.common.exception.ApiException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Every error leaves the API as `application/problem+json` (RFC 9457): Spring MVC's own exceptions through the base
 * class, the domain's [ApiException]s with their status, and validation failures with an `errors` list of
 * `{ field, message }`. Security failures (401/403) are answered by the security filter chain before this runs.
 */
@RestControllerAdvice
class RestExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ProblemDetail = ProblemDetail.forStatusAndDetail(ex.status, ex.message)

    @ExceptionHandler(OptimisticLockingFailureException::class)
    fun handleConcurrentUpdate(): ProblemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT,
        "Someone else changed this at the same time. Reload and try again.",
    )

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val problem = ex.body
        problem.detail = "Some fields are invalid."
        problem.setProperty(
            "errors",
            ex.bindingResult.fieldErrors.map { FieldProblem(it.field, it.defaultMessage ?: "is invalid") },
        )
        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    /** One invalid request field. */
    data class FieldProblem(val field: String, val message: String)
}
