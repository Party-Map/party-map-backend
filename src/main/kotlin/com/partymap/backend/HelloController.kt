package com.partymap.backend

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

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
}
