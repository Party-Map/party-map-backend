package com.partymap.backend.config

import com.partymap.backend.domain.common.exception.NotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.ServletWebRequest

class RestExceptionHandlerTest {
    private val handler = RestExceptionHandler()

    @Suppress("unused")
    private fun target(body: String) = body

    @Test
    fun `domain exceptions keep their status and message`() {
        val problem = handler.handleApiException(NotFoundException("Place 1 was not found."))

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
        assertEquals("Place 1 was not found.", problem.detail)
    }

    @Test
    fun `a concurrent update is a conflict`() {
        assertEquals(HttpStatus.CONFLICT.value(), handler.handleConcurrentUpdate().status)
    }

    @Test
    fun `field errors are listed, with a fallback text when the validator gave none`() {
        val binding = BeanPropertyBindingResult(Any(), "body")
        binding.addError(FieldError("body", "name", "must not be blank"))
        binding.addError(FieldError("body", "city", null, false, null, null, null))
        val parameter = MethodParameter(javaClass.getDeclaredMethod("target", String::class.java), 0)
        val exception = MethodArgumentNotValidException(parameter, binding)

        val response = handler.handleException(exception, ServletWebRequest(MockHttpServletRequest()))!!

        val problem = response.body as ProblemDetail
        assertEquals(
            listOf(
                RestExceptionHandler.FieldProblem("name", "must not be blank"),
                RestExceptionHandler.FieldProblem("city", "is invalid"),
            ),
            problem.properties!!["errors"],
        )
    }
}
