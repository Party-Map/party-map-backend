package com.partymap.backend.domain.user

import com.partymap.backend.domain.common.exception.UnauthorizedException
import com.partymap.backend.support.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class CurrentUserServiceTest : IntegrationTest() {
    @Autowired
    private lateinit var currentUserService: CurrentUserService

    private fun jwt(subject: String): Jwt = Jwt.withTokenValue("token").header("alg", "none").subject(subject).build()

    @Test
    fun `creates the user on the first call and returns the same row afterwards`() {
        val sub = UUID.randomUUID()

        val first = currentUserService.getOrCreateUser(jwt(sub.toString()))
        val second = currentUserService.getOrCreateUser(jwt(sub.toString()))

        assertEquals(sub, first.sub)
        assertEquals(sub, second.sub)
        assertEquals(1, count("user_entity"))
    }

    @Test
    fun `parallel first calls create exactly one user`() {
        val sub = UUID.randomUUID()
        val calls = List(PARALLEL) { Callable { currentUserService.getOrCreateUser(jwt(sub.toString())).sub } }

        val subs = Executors.newFixedThreadPool(PARALLEL).use { pool -> pool.invokeAll(calls).map { it.get() } }

        assertEquals(List(PARALLEL) { sub }, subs)
        assertEquals(1, count("user_entity"))
    }

    @Test
    fun `a subject that is not a UUID is refused`() {
        assertThrows<UnauthorizedException> { currentUserService.getOrCreateUser(jwt("service-account")) }
    }

    private companion object {
        const val PARALLEL = 16
    }
}
