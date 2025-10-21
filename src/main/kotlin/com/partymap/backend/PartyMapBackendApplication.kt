package com.partymap.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PartyMapBackendApplication

fun main(args: Array<String>) {
	runApplication<PartyMapBackendApplication>(*args)
}
