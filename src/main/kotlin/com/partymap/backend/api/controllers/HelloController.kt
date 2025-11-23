package com.partymap.backend.api.controllers

import com.partymap.backend.api.dtos.UserDto
import com.partymap.backend.api.mappers.toDto
import com.partymap.backend.domain.user.service.CurrentUserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class HelloController(private val currentUserService: CurrentUserService) {

    val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/ping")
    fun ping(): Map<String, String> {
        logger.info("Ping requested")
        return mapOf("status" to "pong")
    }

    @GetMapping("/hello")
    fun hello(): Map<String, String> = mapOf("hello" to "world")

    @GetMapping("/jwt-test")
    fun jwtTest(principal: Principal?): Map<String, String> {
        return if (principal != null) {
            mapOf("sub" to principal.name)
        } else {
            mapOf("error" to "no principal")
        }
    }

    @GetMapping("/api/me")
    fun me(@AuthenticationPrincipal jwt: Jwt): UserDto {
        val user = currentUserService.getOrCreateUser(jwt)
        return user.toDto()
    }
}