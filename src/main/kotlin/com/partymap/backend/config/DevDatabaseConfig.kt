package com.partymap.backend.config

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/** In the dev profile every start drops the schema and migrates it again, so the seed data is always fresh. */
@Configuration
@Profile("dev")
class DevDatabaseConfig {
    @Bean
    fun cleanMigrateStrategy() = FlywayMigrationStrategy { flyway ->
        flyway.clean()
        flyway.migrate()
    }
}
