package com.partymap.backend.domain.eventplan

import com.partymap.backend.config.Roles
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationState
import com.partymap.backend.support.IntegrationTest
import com.partymap.backend.support.tokenFor
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDateTime
import java.util.UUID

class EventPlanControllerTest : IntegrationTest() {
    private val organizer = UUID.randomUUID()
    private val asOrganizer get() = tokenFor(organizer, Roles.EVENT_ORGANIZER)
    private val asStranger get() = tokenFor(UUID.randomUUID(), Roles.EVENT_ORGANIZER)

    private fun planBody(
        title: String = "Autumn Rave",
        start: LocalDateTime = LocalDateTime.of(2030, 10, 1, 22, 0),
        end: LocalDateTime = LocalDateTime.of(2030, 10, 2, 5, 0),
    ) = mapOf(
        "title" to title,
        "description" to "",
        "startDateTime" to start.toString(),
        "endDateTime" to end.toString(),
        "image" to null,
        "price" to "5000 HUF",
        "kind" to "TECHNO",
        "links" to listOf(mapOf("type" to "WEBSITE", "url" to "https://rave.example")),
    )

    private fun ownPlan() = data.eventPlan(owner = data.user(organizer))

    @Nested
    inner class Plans {
        @Test
        fun `an organizer creates a plan and reads it back`() {
            val id = mockMvc.post("/api/event-plan") {
                with(asOrganizer)
                json(planBody())
            }.andExpect {
                status { isOk() }
                jsonPath("$.title") { value("Autumn Rave") }
                jsonPath("$.startDateTime") { value("2030-10-01T22:00:00") }
                jsonPath("$.placeInvitation") { value(null as Any?) }
                jsonPath("$.lineupInvitations", hasSize<Any>(0))
            }.andReturn().let { jsonMapper.readTree(it.response.contentAsString).path("id").asString() }

            mockMvc.get("/api/event-plan/$id") { with(asOrganizer) }.andExpect {
                status { isOk() }
                jsonPath("$.links[0].url") { value("https://rave.example") }
                jsonPath("$.kind") { value("TECHNO") }
            }
        }

        @Test
        fun `description, image, price and links are optional`() {
            val body = mapOf(
                "title" to "  Minimal  ",
                "startDateTime" to "2030-10-01T22:00:00",
                "endDateTime" to "2030-10-02T02:00:00",
                "kind" to "PUB",
                "image" to "",
                "price" to " ",
            )
            mockMvc.post("/api/event-plan") {
                with(asOrganizer)
                json(body)
            }.andExpect {
                status { isOk() }
                jsonPath("$.title") { value("Minimal") }
                jsonPath("$.description") { value("") }
                jsonPath("$.image") { value(null as Any?) }
                jsonPath("$.price") { value(null as Any?) }
                jsonPath("$.links", hasSize<Any>(0))
            }
        }

        @Test
        fun `creating a plan needs the organizer role`() {
            mockMvc.post("/api/event-plan") { json(planBody()) }.andExpect { status { isUnauthorized() } }
            mockMvc.post("/api/event-plan") {
                with(tokenFor(organizer, Roles.PLACE_MANAGER))
                json(planBody())
            }.andExpect { status { isForbidden() } }
        }

        @Test
        fun `a plan needs a title and must end after it starts`() {
            mockMvc.post("/api/event-plan") {
                with(asOrganizer)
                json(planBody(title = ""))
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.errors[*].field", contains("title"))
            }
            mockMvc.post("/api/event-plan") {
                with(asOrganizer)
                json(planBody(end = LocalDateTime.of(2030, 10, 1, 21, 0)))
            }.andExpect { status { isBadRequest() } }
            assertEquals(0, count("event_plan_entity"))
        }

        @Test
        fun `only the owner reads and edits a plan`() {
            val plan = ownPlan()

            mockMvc.get("/api/event-plan/${plan.id}") { with(asStranger) }.andExpect { status { isForbidden() } }
            mockMvc.put("/api/event-plan/${plan.id}") {
                with(asStranger)
                json(planBody())
            }.andExpect { status { isForbidden() } }
            mockMvc.put("/api/event-plan/${plan.id}") {
                with(asOrganizer)
                json(planBody(title = "Renamed"))
            }.andExpect {
                status { isOk() }
                jsonPath("$.title") { value("Renamed") }
                jsonPath("$.price") { value("5000 HUF") }
            }
        }

        @Test
        fun `editing a plan checks the times too`() {
            val plan = ownPlan()

            mockMvc.put("/api/event-plan/${plan.id}") {
                with(asOrganizer)
                json(planBody(end = LocalDateTime.of(2030, 10, 1, 22, 0)))
            }.andExpect { status { isBadRequest() } }
        }

        @Test
        fun `an unknown plan is not found`() {
            mockMvc.get("/api/event-plan/${UUID.randomUUID()}") { with(asOrganizer) }
                .andExpect { status { isNotFound() } }
            mockMvc.put("/api/event-plan/${UUID.randomUUID()}") {
                with(asOrganizer)
                json(planBody())
            }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `owned plans are the caller's plans`() {
            ownPlan()
            data.eventPlan(title = "Theirs")

            mockMvc.get("/api/event-plan/owned-event-plans") { with(asOrganizer) }.andExpect {
                status { isOk() }
                jsonPath("$[*].title", contains("Summer Opening"))
                jsonPath("$[0].startDateTime") { exists() }
            }
        }

        @Test
        fun `organizers pick from every place`() {
            data.place(name = "One")
            data.place(name = "Two")

            mockMvc.get("/api/event-plan/places") { with(asOrganizer) }.andExpect {
                status { isOk() }
                jsonPath("$[*].name", containsInAnyOrder("One", "Two"))
            }
        }
    }

    @Nested
    inner class PlaceInvitation {
        @Test
        fun `the owner invites a place`() {
            val plan = ownPlan()
            val place = data.place()

            mockMvc.put("/api/event-plan/${plan.id}/invite-place/${place.id}") { with(asOrganizer) }
                .andExpect { status { isNoContent() } }

            mockMvc.get("/api/event-plan/${plan.id}") { with(asOrganizer) }.andExpect {
                jsonPath("$.placeInvitation.state") { value("PENDING") }
                jsonPath("$.placeInvitation.place.id") { value(place.id.toString()) }
            }
        }

        @Test
        fun `inviting another place replaces the invitation`() {
            val plan = ownPlan()
            data.placeInvitation(plan, data.place(name = "First"), EventPlanPlaceInvitationState.ACCEPTED)
            val second = data.place(name = "Second")

            mockMvc.put("/api/event-plan/${plan.id}/invite-place/${second.id}") { with(asOrganizer) }
                .andExpect { status { isNoContent() } }

            assertEquals(1, count("event_plan_place_invitation_entity"))
            mockMvc.get("/api/event-plan/${plan.id}") { with(asOrganizer) }.andExpect {
                jsonPath("$.placeInvitation.place.name") { value("Second") }
                jsonPath("$.placeInvitation.state") { value("PENDING") }
            }
        }

        @Test
        fun `inviting the same place again asks it again`() {
            val plan = ownPlan()
            val place = data.place()
            data.placeInvitation(plan, place, EventPlanPlaceInvitationState.REJECTED)

            mockMvc.put("/api/event-plan/${plan.id}/invite-place/${place.id}") { with(asOrganizer) }
                .andExpect { status { isNoContent() } }

            mockMvc.get("/api/event-plan/${plan.id}") { with(asOrganizer) }.andExpect {
                jsonPath("$.placeInvitation.state") { value("PENDING") }
            }
        }

        @Test
        fun `only the owner invites a place`() {
            val plan = ownPlan()
            val place = data.place()

            mockMvc.put("/api/event-plan/${plan.id}/invite-place/${place.id}") { with(asStranger) }
                .andExpect { status { isForbidden() } }
            assertEquals(0, count("event_plan_place_invitation_entity"))
        }

        @Test
        fun `inviting an unknown place or into an unknown plan is not found`() {
            val plan = ownPlan()

            mockMvc.put("/api/event-plan/${plan.id}/invite-place/${UUID.randomUUID()}") { with(asOrganizer) }
                .andExpect { status { isNotFound() } }
            mockMvc.put("/api/event-plan/${UUID.randomUUID()}/invite-place/${data.place().id}") {
                with(asOrganizer)
            }.andExpect { status { isNotFound() } }
        }
    }

    @Nested
    inner class Lineup {
        private fun slot(performerId: UUID?, start: LocalDateTime, end: LocalDateTime = start.plusHours(1)) =
            mapOf("performerId" to performerId, "startTime" to start.toString(), "endTime" to end.toString())

        @Test
        fun `the owner invites performers and lists them`() {
            val plan = ownPlan()
            val performer = data.performer()

            mockMvc.post("/api/event-plan/${plan.id}/add-lineup-invitation") {
                with(asOrganizer)
                json(slot(performer.id, plan.startDateTime))
            }.andExpect { status { isNoContent() } }

            mockMvc.get("/api/event-plan/${plan.id}/lineup-invitations") { with(asOrganizer) }.andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].state") { value("PENDING") }
                jsonPath("$[0].performer.id") { value(performer.id.toString()) }
                jsonPath("$[0].startTime") { value(iso(plan.startDateTime)) }
            }
        }

        @Test
        fun `a performer is invited once`() {
            val plan = ownPlan()
            val performer = data.performer()
            data.lineupInvitation(plan, performer)

            mockMvc.post("/api/event-plan/${plan.id}/add-lineup-invitation") {
                with(asOrganizer)
                json(slot(performer.id, plan.startDateTime.plusHours(2)))
            }.andExpect { status { isConflict() } }
        }

        @Test
        fun `a slot must end after it starts and stay inside the plan`() {
            val plan = ownPlan()
            val performer = data.performer()
            val invalidSlots = listOf(
                slot(performer.id, plan.startDateTime, plan.startDateTime),
                slot(performer.id, plan.startDateTime.plusHours(2), plan.startDateTime.plusHours(1)),
                slot(performer.id, plan.startDateTime.minusHours(1)),
                slot(performer.id, plan.endDateTime.minusMinutes(30)),
            )

            invalidSlots.forEach { body ->
                mockMvc.post("/api/event-plan/${plan.id}/add-lineup-invitation") {
                    with(asOrganizer)
                    json(body)
                }.andExpect { status { isBadRequest() } }
            }
            assertEquals(0, count("event_plan_lineup_invitation_entity"))
        }

        @Test
        fun `a slot without a performer is rejected`() {
            val plan = ownPlan()

            mockMvc.post("/api/event-plan/${plan.id}/add-lineup-invitation") {
                with(asOrganizer)
                json(slot(null, plan.startDateTime))
            }.andExpect { status { isBadRequest() } }
        }

        @Test
        fun `inviting an unknown performer is not found`() {
            val plan = ownPlan()

            mockMvc.post("/api/event-plan/${plan.id}/add-lineup-invitation") {
                with(asOrganizer)
                json(slot(UUID.randomUUID(), plan.startDateTime))
            }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `only the owner changes or reads the lineup`() {
            val plan = ownPlan()
            val performer = data.performer()
            data.lineupInvitation(plan, performer)

            mockMvc.post("/api/event-plan/${plan.id}/add-lineup-invitation") {
                with(asStranger)
                json(slot(data.performer().id, plan.startDateTime))
            }.andExpect { status { isForbidden() } }
            mockMvc.get("/api/event-plan/${plan.id}/lineup-invitations") { with(asStranger) }
                .andExpect { status { isForbidden() } }
            mockMvc.delete("/api/event-plan/${plan.id}/lineup-invitation/${performer.id}") { with(asStranger) }
                .andExpect { status { isForbidden() } }
            assertEquals(1, count("event_plan_lineup_invitation_entity"))
        }

        @Test
        fun `a removed performer can be invited again`() {
            val plan = ownPlan()
            val performer = data.performer()
            data.lineupInvitation(plan, performer)

            mockMvc.delete("/api/event-plan/${plan.id}/lineup-invitation/${performer.id}") { with(asOrganizer) }
                .andExpect { status { isNoContent() } }
            assertEquals(0, count("event_plan_lineup_invitation_entity"))

            mockMvc.post("/api/event-plan/${plan.id}/add-lineup-invitation") {
                with(asOrganizer)
                json(slot(performer.id, plan.startDateTime))
            }.andExpect { status { isNoContent() } }
            assertEquals(1, count("event_plan_lineup_invitation_entity"))
        }

        @Test
        fun `removing a performer that is not in the lineup is not found`() {
            val plan = ownPlan()

            mockMvc.delete("/api/event-plan/${plan.id}/lineup-invitation/${UUID.randomUUID()}") { with(asOrganizer) }
                .andExpect { status { isNotFound() } }
        }
    }

    @Nested
    inner class Publishing {
        @Test
        fun `publishing turns the plan into an event with the accepted lineup`() {
            val plan = ownPlan()
            val place = data.place(name = "Venue")
            val accepted = data.performer(name = "Accepted")
            val rejected = data.performer(name = "Rejected")
            data.placeInvitation(plan, place, EventPlanPlaceInvitationState.ACCEPTED)
            data.lineupInvitation(plan, accepted, EventPlanLineupInvitationState.ACCEPTED)
            data.lineupInvitation(plan, rejected, EventPlanLineupInvitationState.REJECTED)

            mockMvc.post("/api/event-plan/${plan.id}/publish") { with(asOrganizer) }
                .andExpect { status { isNoContent() } }

            mockMvc.get("/api/event-plan/${plan.id}") { with(asOrganizer) }.andExpect { status { isNotFound() } }
            assertEquals(0, count("event_plan_place_invitation_entity"))
            assertEquals(0, count("event_plan_lineup_invitation_entity"))
            mockMvc.get("/api/events?placeId=${place.id}").andExpect {
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].title") { value("Summer Opening") }
                jsonPath("$[0].links[0].url") { value("https://summer.example") }
                jsonPath("$[0].lineupItems[*].performer.name", contains("Accepted"))
            }
            mockMvc.get("/api/events?performerId=${accepted.id}").andExpect {
                jsonPath("$[*].title", contains("Summer Opening"))
            }
            mockMvc.get("/api/events/owned-events") { with(asOrganizer) }.andExpect {
                jsonPath("$[*].placeName", contains("Venue"))
            }
        }

        @Test
        fun `a plan without an accepted place cannot be published`() {
            val plan = ownPlan()
            data.placeInvitation(plan, data.place(), EventPlanPlaceInvitationState.PENDING)

            mockMvc.post("/api/event-plan/${plan.id}/publish") { with(asOrganizer) }.andExpect {
                status { isConflict() }
                jsonPath("$.detail") { exists() }
            }
            assertEquals(0, count("event_entity"))
        }

        @Test
        fun `a plan with a pending performer cannot be published`() {
            val plan = ownPlan()
            data.placeInvitation(plan, data.place(), EventPlanPlaceInvitationState.ACCEPTED)
            data.lineupInvitation(plan, data.performer())

            mockMvc.post("/api/event-plan/${plan.id}/publish") { with(asOrganizer) }
                .andExpect { status { isConflict() } }
            assertEquals(1, count("event_plan_entity"))
        }

        @Test
        fun `only the owner publishes`() {
            val plan = ownPlan()
            data.placeInvitation(plan, data.place(), EventPlanPlaceInvitationState.ACCEPTED)

            mockMvc.post("/api/event-plan/${plan.id}/publish") { with(asStranger) }
                .andExpect { status { isForbidden() } }
            mockMvc.post("/api/event-plan/${UUID.randomUUID()}/publish") { with(asOrganizer) }
                .andExpect { status { isNotFound() } }
            assertEquals(0, count("event_entity"))
        }

        @Test
        fun `the place manager's answer is what publishing sees`() {
            val plan = ownPlan()
            val manager = data.user()
            val place = data.place(manager)
            mockMvc.put("/api/event-plan/${plan.id}/invite-place/${place.id}") { with(asOrganizer) }

            mockMvc.put("/api/places/${place.id}/invitations/${plan.id}/respond?state=accept") {
                with(tokenFor(manager.sub, Roles.PLACE_MANAGER))
            }.andExpect { status { isNoContent() } }

            mockMvc.post("/api/event-plan/${plan.id}/publish") { with(asOrganizer) }
                .andExpect { status { isNoContent() } }
            assertEquals(1, count("event_entity"))
        }
    }
}
