package com.partymap.backend.web

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class HelloController {

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
}