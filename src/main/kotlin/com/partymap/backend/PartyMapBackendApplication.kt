package com.partymap.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class PartyMapBackendApplication

fun main(args: Array<String>) {
	runApplication<PartyMapBackendApplication>(*args)
}
