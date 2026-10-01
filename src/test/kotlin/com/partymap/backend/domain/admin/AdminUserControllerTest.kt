package com.partymap.backend.domain.admin

import com.partymap.backend.config.Roles
import com.partymap.backend.domain.common.exception.UpstreamException
import com.partymap.backend.support.IntegrationTest
import com.partymap.backend.support.tokenFor
import org.hamcrest.Matchers.contains
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import java.util.UUID

class AdminUserControllerTest : IntegrationTest() {
    private val admin = tokenFor(UUID.randomUUID(), Roles.PARTYMAP_ADMIN)

    @Nested
    inner class Access {
        @Test
        fun `anonymous callers are unauthorized`() {
            mockMvc.get("/api/admin/users").andExpect { status { isUnauthorized() } }
            mockMvc.put("/api/admin/users/${UUID.randomUUID()}/roles/${Roles.PLACE_MANAGER}")
                .andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `managers without the platform admin role are forbidden`() {
            val user = keycloak.user("jane@example.com")
            val manager = tokenFor(UUID.randomUUID(), *Roles.MANAGER_ROLES.toTypedArray())

            mockMvc.get("/api/admin/users/${user.id}") { with(manager) }.andExpect { status { isForbidden() } }
            mockMvc.put("/api/admin/users/${user.id}/roles/${Roles.PLACE_MANAGER}") { with(manager) }
                .andExpect { status { isForbidden() } }

            assertEquals(emptySet<String>(), keycloak.rolesOf(user.id))
        }
    }

    @Nested
    inner class Listing {
        @Test
        fun `lists users with only the roles the admin pages manage`() {
            val jane = keycloak.user(
                "jane@example.com",
                Roles.USER,
                "offline_access",
                Roles.PLACE_MANAGER,
                Roles.PARTYMAP_ADMIN,
                firstName = "Jane",
                lastName = "Doe",
            )
            keycloak.user("bob@example.com", enabled = false)

            mockMvc.get("/api/admin/users") { with(admin) }.andExpect {
                status { isOk() }
                jsonPath("$.total") { value(2) }
                jsonPath("$.page") { value(0) }
                jsonPath("$.size") { value(20) }
                jsonPath("$.items[0].id") { value(jane.id.toString()) }
                jsonPath("$.items[0].username") { value("jane@example.com") }
                jsonPath("$.items[0].email") { value("jane@example.com") }
                jsonPath("$.items[0].firstName") { value("Jane") }
                jsonPath("$.items[0].lastName") { value("Doe") }
                jsonPath("$.items[0].enabled") { value(true) }
                jsonPath("$.items[0].roles", contains(Roles.PARTYMAP_ADMIN, Roles.PLACE_MANAGER))
                jsonPath("$.items[1].enabled") { value(false) }
                jsonPath("$.items[1].roles.length()") { value(0) }
            }
            assertEquals(Triple(null, 0, 20), keycloak.lastSearch)
        }

        @Test
        fun `passes the trimmed search and the page through and counts the matches`() {
            keycloak.user("adam@example.com")
            keycloak.user("adrian@szell.dev")
            keycloak.user("ada@example.com")
            keycloak.user("zoe@example.com")

            mockMvc.get("/api/admin/users?q= ad &page=1&size=2") { with(admin) }.andExpect {
                status { isOk() }
                jsonPath("$.total") { value(3) }
                jsonPath("$.page") { value(1) }
                jsonPath("$.size") { value(2) }
                jsonPath("$.items.length()") { value(1) }
                jsonPath("$.items[0].username") { value("ada@example.com") }
            }
            assertEquals(Triple("ad", 2, 2), keycloak.lastSearch)
        }

        @Test
        fun `a blank search lists everyone`() {
            keycloak.user("a@example.com")

            mockMvc.get("/api/admin/users") {
                with(admin)
                param("q", "  ")
            }.andExpect { jsonPath("$.total") { value(1) } }
            assertEquals(Triple(null, 0, 20), keycloak.lastSearch)
        }

        @Test
        fun `rejects page sizes and pages out of range, naming the parameter`() {
            for (query in listOf("size=51", "size=0", "page=-1")) {
                mockMvc.get("/api/admin/users?$query") { with(admin) }.andExpect {
                    status { isBadRequest() }
                    content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                    jsonPath("$.errors[0].field") { value(query.substringBefore('=')) }
                }
            }
        }

        @Test
        fun `reads one user`() {
            val user = keycloak.user("jane@example.com", Roles.EVENT_ORGANIZER)

            mockMvc.get("/api/admin/users/${user.id}") { with(admin) }.andExpect {
                status { isOk() }
                jsonPath("$.username") { value("jane@example.com") }
                jsonPath("$.roles", contains(Roles.EVENT_ORGANIZER))
            }
        }

        @Test
        fun `an unknown user is not found`() {
            val id = UUID.randomUUID()

            mockMvc.get("/api/admin/users/$id") { with(admin) }.andExpect {
                status { isNotFound() }
                jsonPath("$.detail") { value("User $id was not found.") }
            }
            mockMvc.put("/api/admin/users/$id/roles/${Roles.PLACE_MANAGER}") { with(admin) }
                .andExpect { status { isNotFound() } }
        }

        @Test
        fun `a malformed user id is a bad request`() {
            mockMvc.get("/api/admin/users/not-a-uuid") { with(admin) }.andExpect { status { isBadRequest() } }
        }
    }

    @Nested
    inner class RoleChanges {
        @Test
        fun `grants each manager role once, however often it is asked`() {
            val user = keycloak.user("jane@example.com", Roles.USER)

            for (role in Roles.MANAGER_ROLES) {
                repeat(2) {
                    mockMvc.put("/api/admin/users/${user.id}/roles/$role") { with(admin) }
                        .andExpect { status { isNoContent() } }
                }
            }

            assertEquals(Roles.MANAGER_ROLES + Roles.USER, keycloak.rolesOf(user.id))
        }

        @Test
        fun `revokes a manager role and accepts revoking it again`() {
            val user = keycloak.user("jane@example.com", Roles.USER, Roles.PLACE_MANAGER, Roles.EVENT_ORGANIZER)

            repeat(2) {
                mockMvc.delete("/api/admin/users/${user.id}/roles/${Roles.PLACE_MANAGER}") { with(admin) }
                    .andExpect { status { isNoContent() } }
            }

            assertEquals(setOf(Roles.USER, Roles.EVENT_ORGANIZER), keycloak.rolesOf(user.id))
        }

        @Test
        fun `only the manager roles can be granted or revoked`() {
            val user = keycloak.user("jane@example.com", Roles.USER, Roles.PARTYMAP_ADMIN)

            for (role in listOf(Roles.PARTYMAP_ADMIN, Roles.USER, "realm-admin")) {
                mockMvc.put("/api/admin/users/${user.id}/roles/$role") { with(admin) }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.detail") {
                        value(
                            "Only the manager roles can be granted or revoked: " +
                                "event_organizer_user, performer_manager_user, place_manager_user.",
                        )
                    }
                }
            }
            mockMvc.delete("/api/admin/users/${user.id}/roles/${Roles.PARTYMAP_ADMIN}") { with(admin) }
                .andExpect { status { isBadRequest() } }

            assertEquals(setOf(Roles.USER, Roles.PARTYMAP_ADMIN), keycloak.rolesOf(user.id))
        }
    }

    @Test
    fun `Keycloak failures are a bad gateway with the reason`() {
        keycloak.failure = UpstreamException("The Keycloak admin client is not configured.")

        mockMvc.get("/api/admin/users") { with(admin) }.andExpect {
            status { isBadGateway() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.detail") { value("The Keycloak admin client is not configured.") }
        }
    }
}
