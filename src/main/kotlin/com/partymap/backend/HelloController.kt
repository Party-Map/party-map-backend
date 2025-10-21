package com.partymap.backend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.access.prepost.PreAuthorize

@RestController
class HelloController {
    @GetMapping("/ping")
    fun ping(): Map<String, String> = mapOf("status" to "pong")
}
