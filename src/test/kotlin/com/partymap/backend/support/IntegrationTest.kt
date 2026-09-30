package com.partymap.backend.support

import com.partymap.backend.TestcontainersConfig
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Base for tests that need the application context: a real Postgres (Testcontainers), the Flyway schema and MockMvc.
 *
 * Tests are deliberately not wrapped in a transaction: every request commits like in production, so lazy loading,
 * constraint and concurrency problems surface. The tables are emptied after each test instead.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfig::class, TestData::class)
abstract class IntegrationTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var data: TestData

    @Autowired
    protected lateinit var jdbc: JdbcTemplate

    @Autowired
    protected lateinit var jsonMapper: JsonMapper

    @AfterEach
    fun emptyTables() {
        val tables = jdbc.queryForList(
            "SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'",
            String::class.java,
        )
        if (tables.isNotEmpty()) {
            jdbc.execute("TRUNCATE TABLE ${tables.joinToString { "\"$it\"" }} CASCADE")
        }
    }

    /** Sends [body] as the JSON request body. */
    protected fun MockHttpServletRequestDsl.json(body: Any) {
        contentType = MediaType.APPLICATION_JSON
        content = jsonMapper.writeValueAsString(body)
    }

    /** How the API writes a date-time: ISO-8601 with seconds. */
    protected fun iso(value: LocalDateTime): String = value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    protected fun count(table: String): Int = jdbc.queryForObject("SELECT count(*) FROM $table", Int::class.java)!!
}
