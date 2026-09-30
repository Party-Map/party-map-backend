package com.partymap.backend.domain.performer

import com.partymap.backend.config.Roles
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.support.IntegrationTest
import com.partymap.backend.support.tokenFor
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.UUID

class PerformerControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var performers: PerformerRepository

    private fun performerBody(name: String = "Kistehén", genre: String = "Indie") = mapOf(
        "name" to name,
        "genre" to genre,
        "bio" to "Budapest band",
        "image" to null,
        "links" to listOf(mapOf("type" to "INSTAGRAM", "url" to "https://instagram.com/kistehen")),
    )

    @Nested
    inner class Reading {
        @Test
        fun `lists every performer`() {
            data.performer(name = "A")
            data.performer(name = "B")

            mockMvc.get("/api/performers").andExpect {
                status { isOk() }
                jsonPath("$[*].name", containsInAnyOrder("A", "B"))
                jsonPath("$[0].links[0].type") { value("INSTAGRAM") }
            }
        }

        @Test
        fun `returns one performer`() {
            val performer = data.performer()

            mockMvc.get("/api/performers/${performer.id}").andExpect {
                status { isOk() }
                jsonPath("$.name") { value("DJ Pond") }
                jsonPath("$.genre") { value("Techno") }
                jsonPath("$.bio") { value("Plays until sunrise") }
            }
        }

        @Test
        fun `an unknown performer is not found`() {
            mockMvc.get("/api/performers/${UUID.randomUUID()}").andExpect { status { isNotFound() } }
        }

        @Test
        fun `liked performers need a signed-in user`() {
            mockMvc.get("/api/performers/liked-performers").andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `liked performers are the caller's likes`() {
            val sub = UUID.randomUUID()
            val liked = data.performer(name = "Liked")
            data.performer(name = "Other")
            mockMvc.put("/api/me/likes/performers/${liked.id}") { with(tokenFor(sub)) }

            mockMvc.get("/api/performers/liked-performers") { with(tokenFor(sub)) }.andExpect {
                status { isOk() }
                jsonPath("$[*].name", containsInAnyOrder("Liked"))
            }
        }

        @Test
        fun `owned performers need the performer manager role and list only the caller's`() {
            val owner = data.user()
            data.performer(owner, name = "Mine")
            data.performer(name = "Theirs")

            mockMvc.get("/api/performers/owned-performers") { with(tokenFor(owner.sub)) }
                .andExpect { status { isForbidden() } }
            mockMvc.get("/api/performers/owned-performers") { with(tokenFor(owner.sub, Roles.PERFORMER_MANAGER)) }
                .andExpect {
                    status { isOk() }
                    jsonPath("$[*].name", containsInAnyOrder("Mine"))
                }
        }
    }

    @Nested
    inner class Writing {
        @Test
        fun `a performer manager creates a performer`() {
            val sub = UUID.randomUUID()

            mockMvc.post("/api/performers") {
                with(tokenFor(sub, Roles.PERFORMER_MANAGER))
                json(performerBody())
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Kistehén") }
                jsonPath("$.image") { value(null as Any?) }
                jsonPath("$.links[0].url") { value("https://instagram.com/kistehen") }
            }
            assertEquals(listOf("Kistehén"), performers.findAllByOwnerSub(sub).map { it.name })
        }

        @Test
        fun `bio, image and links may be left out`() {
            mockMvc.post("/api/performers") {
                with(tokenFor(UUID.randomUUID(), Roles.PERFORMER_MANAGER))
                json(mapOf("name" to "Quiet", "genre" to "Ambient", "image" to "  "))
            }.andExpect {
                status { isOk() }
                jsonPath("$.bio") { value("") }
                jsonPath("$.image") { value(null as Any?) }
                jsonPath("$.links", hasSize<Any>(0))
            }
        }

        @Test
        fun `creating a performer needs the performer manager role`() {
            mockMvc.post("/api/performers") {
                with(tokenFor(UUID.randomUUID(), Roles.PLACE_MANAGER))
                json(performerBody())
            }.andExpect { status { isForbidden() } }
        }

        @Test
        fun `a performer without name and genre is rejected`() {
            mockMvc.post("/api/performers") {
                with(tokenFor(UUID.randomUUID(), Roles.PERFORMER_MANAGER))
                json(performerBody(name = "", genre = ""))
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.errors[*].field", containsInAnyOrder("name", "genre"))
            }
        }

        @Test
        fun `the owner updates the performer`() {
            val owner = data.user()
            val performer = data.performer(owner)

            mockMvc.put("/api/performers/${performer.id}") {
                with(tokenFor(owner.sub, Roles.PERFORMER_MANAGER))
                json(performerBody(name = "Renamed"))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Renamed") }
                jsonPath("$.genre") { value("Indie") }
            }
        }

        @Test
        fun `another performer manager may not edit the performer`() {
            val performer = data.performer()

            mockMvc.put("/api/performers/${performer.id}") {
                with(tokenFor(UUID.randomUUID(), Roles.PERFORMER_MANAGER))
                json(performerBody())
            }.andExpect { status { isForbidden() } }
        }

        @Test
        fun `updating an unknown performer is not found`() {
            mockMvc.put("/api/performers/${UUID.randomUUID()}") {
                with(tokenFor(UUID.randomUUID(), Roles.PERFORMER_MANAGER))
                json(performerBody())
            }.andExpect { status { isNotFound() } }
        }
    }

    @Nested
    inner class Invitations {
        @Test
        fun `the owner sees the lineup invitations of the performer only`() {
            val owner = data.user()
            val performer = data.performer(owner)
            val plan = data.eventPlan(title = "Opening")
            data.lineupInvitation(plan, performer)
            data.lineupInvitation(data.eventPlan(title = "Other"), data.performer(name = "Someone else"))

            mockMvc.get("/api/performers/${performer.id}/invitations") {
                with(tokenFor(owner.sub, Roles.PERFORMER_MANAGER))
            }.andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].eventPlanId") { value(plan.id.toString()) }
                jsonPath("$[0].eventPlanTitle") { value("Opening") }
                jsonPath("$[0].state") { value("PENDING") }
                jsonPath("$[0].performer.name") { value("DJ Pond") }
            }
        }

        @Test
        fun `another performer manager may not see the invitations`() {
            val performer = data.performer()

            mockMvc.get("/api/performers/${performer.id}/invitations") {
                with(tokenFor(UUID.randomUUID(), Roles.PERFORMER_MANAGER))
            }.andExpect { status { isForbidden() } }
        }

        @Test
        fun `invitations of an unknown performer are not found`() {
            mockMvc.get("/api/performers/${UUID.randomUUID()}/invitations") {
                with(tokenFor(UUID.randomUUID(), Roles.PERFORMER_MANAGER))
            }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `the owner accepts and rejects an invitation`() {
            val owner = data.user()
            val performer = data.performer(owner)
            val plan = data.eventPlan()
            data.lineupInvitation(plan, performer)
            val url = "/api/performers/${performer.id}/invitations/${plan.id}/respond"

            mockMvc.put("$url?state=accept") { with(tokenFor(owner.sub, Roles.PERFORMER_MANAGER)) }
                .andExpect { status { isNoContent() } }
            assertEquals(EventPlanLineupInvitationState.ACCEPTED.name, invitationState())

            mockMvc.put("$url?state=REJECT") { with(tokenFor(owner.sub, Roles.PERFORMER_MANAGER)) }
                .andExpect { status { isNoContent() } }
            assertEquals(EventPlanLineupInvitationState.REJECTED.name, invitationState())
        }

        @Test
        fun `an unknown answer is a bad request`() {
            val owner = data.user()
            val performer = data.performer(owner)
            val plan = data.eventPlan()
            data.lineupInvitation(plan, performer)

            mockMvc.put("/api/performers/${performer.id}/invitations/${plan.id}/respond?state=maybe") {
                with(tokenFor(owner.sub, Roles.PERFORMER_MANAGER))
            }.andExpect { status { isBadRequest() } }
        }

        @Test
        fun `another performer manager may not answer`() {
            val performer = data.performer()
            val plan = data.eventPlan()
            data.lineupInvitation(plan, performer)

            mockMvc.put("/api/performers/${performer.id}/invitations/${plan.id}/respond?state=accept") {
                with(tokenFor(UUID.randomUUID(), Roles.PERFORMER_MANAGER))
            }.andExpect { status { isForbidden() } }
            assertEquals(EventPlanLineupInvitationState.PENDING.name, invitationState())
        }

        @Test
        fun `answering an invitation that does not exist is not found`() {
            val owner = data.user()
            val performer = data.performer(owner)

            mockMvc.put("/api/performers/${performer.id}/invitations/${data.eventPlan().id}/respond?state=accept") {
                with(tokenFor(owner.sub, Roles.PERFORMER_MANAGER))
            }.andExpect { status { isNotFound() } }
            mockMvc.put("/api/performers/${performer.id}/invitations/${UUID.randomUUID()}/respond?state=accept") {
                with(tokenFor(owner.sub, Roles.PERFORMER_MANAGER))
            }.andExpect { status { isNotFound() } }
        }

        private fun invitationState(): String =
            jdbc.queryForObject("SELECT state FROM event_plan_lineup_invitation_entity", String::class.java)!!
    }
}
