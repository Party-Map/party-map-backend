package com.partymap.backend.domain.place

import com.partymap.backend.config.Roles
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationState
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.support.IntegrationTest
import com.partymap.backend.support.tokenFor
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.UUID

class PlaceControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var places: PlaceRepository

    private fun placeBody(name: String = "Instant", latitude: Double = 47.5, longitude: Double = 19.06) = mapOf(
        "name" to name,
        "location" to mapOf("latitude" to latitude, "longitude" to longitude),
        "address" to "Nagymező utca 38",
        "city" to "Budapest",
        "description" to "Ruin bar",
        "image" to "https://example.com/instant.jpg",
        "tags" to listOf("bar", "ruin"),
        "links" to listOf(mapOf("type" to "WEBSITE", "url" to "https://instant.hu")),
    )

    @Nested
    inner class Reading {
        @Test
        fun `lists every place with its location, tags and links`() {
            val place = data.place(tags = setOf("club", "techno"))

            mockMvc.get("/api/places").andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].id") { value(place.id.toString()) }
                jsonPath("$[0].name") { value("Akvárium Klub") }
                jsonPath("$[0].location.latitude") { value(47.4979) }
                jsonPath("$[0].location.longitude") { value(19.0402) }
                jsonPath("$[0].tags", containsInAnyOrder("club", "techno"))
                jsonPath("$[0].links[0].type") { value("WEBSITE") }
            }
        }

        @Test
        fun `a bounding box limits the list to the places inside it`() {
            val owner = data.user()
            data.place(owner, name = "Budapest", latitude = 47.49, longitude = 19.04)
            data.place(owner, name = "Szeged", latitude = 46.25, longitude = 20.15)

            mockMvc.get("/api/places?bbox=18.9,47.3,19.3,47.7").andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].name") { value("Budapest") }
            }
        }

        @Test
        fun `a malformed bounding box is a bad request`() {
            listOf("1,2,3", "a,b,c,d", "19.3,47.3,18.9,47.7", "18.9,95,19.3,96").forEach { bbox ->
                mockMvc.get("/api/places?bbox=$bbox").andExpect {
                    status { isBadRequest() }
                    content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                }
            }
        }

        @Test
        fun `returns one place`() {
            val place = data.place(description = null)

            mockMvc.get("/api/places/${place.id}").andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Akvárium Klub") }
                jsonPath("$.city") { value("Budapest") }
                jsonPath("$.description") { value(null as Any?) }
            }
        }

        @Test
        fun `an unknown place is not found`() {
            mockMvc.get("/api/places/${UUID.randomUUID()}").andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                jsonPath("$.status") { value(404) }
                jsonPath("$.detail") { exists() }
            }
        }

        @Test
        fun `an id that is not a UUID is a bad request`() {
            mockMvc.get("/api/places/not-a-uuid").andExpect { status { isBadRequest() } }
        }

        @Test
        fun `liked places need a signed-in user`() {
            mockMvc.get("/api/places/liked-places").andExpect {
                status { isUnauthorized() }
                header { string("WWW-Authenticate", startsWith("Bearer")) }
            }
        }

        @Test
        fun `liked places are the caller's likes`() {
            val sub = UUID.randomUUID()
            val liked = data.place(name = "Liked")
            data.place(name = "Other")
            mockMvc.put("/api/me/likes/places/${liked.id}") { with(tokenFor(sub)) }

            mockMvc.get("/api/places/liked-places") { with(tokenFor(sub)) }.andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].name") { value("Liked") }
            }
        }

        @Test
        fun `owned places need the place manager role`() {
            mockMvc.get("/api/places/owned-places") { with(tokenFor(UUID.randomUUID())) }
                .andExpect { status { isForbidden() } }
        }

        @Test
        fun `owned places are the caller's places`() {
            val owner = data.user()
            data.place(owner, name = "Mine")
            data.place(name = "Theirs")

            mockMvc.get("/api/places/owned-places") { with(tokenFor(owner.sub, Roles.PLACE_MANAGER)) }.andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].name") { value("Mine") }
                jsonPath("$[0].address") { value("Erzsébet tér 12") }
                jsonPath("$[0].city") { value("Budapest") }
            }
        }
    }

    @Nested
    inner class Writing {
        @Test
        fun `creating a place needs a token`() {
            mockMvc.post("/api/places") { json(placeBody()) }.andExpect { status { isUnauthorized() } }
        }

        @Test
        fun `creating a place needs the place manager role`() {
            mockMvc.post("/api/places") {
                with(tokenFor(UUID.randomUUID(), Roles.EVENT_ORGANIZER))
                json(placeBody())
            }.andExpect { status { isForbidden() } }
        }

        @Test
        fun `a place manager creates a place they own`() {
            val sub = UUID.randomUUID()

            mockMvc.post("/api/places") {
                with(tokenFor(sub, Roles.PLACE_MANAGER))
                json(placeBody())
            }.andExpect {
                status { isOk() }
                jsonPath("$.id") { exists() }
                jsonPath("$.name") { value("Instant") }
                jsonPath("$.tags", containsInAnyOrder("bar", "ruin"))
                jsonPath("$.links[0].url") { value("https://instant.hu") }
            }

            val saved = places.findAllByOwnerSub(sub)
            assertEquals(listOf("Instant"), saved.map { it.name })
        }

        @Test
        fun `optional fields may be left out`() {
            val body = mapOf(
                "name" to "Bare",
                "location" to mapOf("latitude" to 47.5, "longitude" to 19.0),
                "address" to "",
                "city" to "Budapest",
            )
            mockMvc.post("/api/places") {
                with(tokenFor(UUID.randomUUID(), Roles.PLACE_MANAGER))
                json(body)
            }.andExpect {
                status { isOk() }
                jsonPath("$.tags", hasSize<Any>(0))
                jsonPath("$.links", hasSize<Any>(0))
            }
        }

        @Test
        fun `an invalid place is rejected with the field errors`() {
            mockMvc.post("/api/places") {
                with(tokenFor(UUID.randomUUID(), Roles.PLACE_MANAGER))
                json(placeBody(name = " ", latitude = 91.0))
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                jsonPath("$.errors[*].field", containsInAnyOrder("name", "location.latitude"))
            }
            assertEquals(0, count("place_entity"))
        }

        @Test
        fun `a body that is not JSON is a bad request`() {
            mockMvc.post("/api/places") {
                with(tokenFor(UUID.randomUUID(), Roles.PLACE_MANAGER))
                contentType = MediaType.APPLICATION_JSON
                content = "{not json"
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            }
        }

        @Test
        fun `the owner updates every field of a place`() {
            val owner = data.user()
            val place = data.place(owner)

            mockMvc.put("/api/places/${place.id}") {
                with(tokenFor(owner.sub, Roles.PLACE_MANAGER))
                json(placeBody(name = "Renamed"))
            }.andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Renamed") }
                jsonPath("$.tags", containsInAnyOrder("bar", "ruin"))
                jsonPath("$.links[0].url") { value("https://instant.hu") }
            }

            mockMvc.get("/api/places/${place.id}").andExpect {
                jsonPath("$.name") { value("Renamed") }
                jsonPath("$.location.latitude") { value(47.5) }
                jsonPath("$.links", hasSize<Any>(1))
            }
        }

        @Test
        fun `another place manager may not edit the place`() {
            val place = data.place()

            mockMvc.put("/api/places/${place.id}") {
                with(tokenFor(UUID.randomUUID(), Roles.PLACE_MANAGER))
                json(placeBody(name = "Hijacked"))
            }.andExpect {
                status { isForbidden() }
                content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            }
            assertEquals("Akvárium Klub", places.findById(place.id!!).orElseThrow().name)
        }

        @Test
        fun `updating an unknown place is not found`() {
            mockMvc.put("/api/places/${UUID.randomUUID()}") {
                with(tokenFor(UUID.randomUUID(), Roles.PLACE_MANAGER))
                json(placeBody())
            }.andExpect { status { isNotFound() } }
        }
    }

    @Nested
    inner class Invitations {
        @Test
        fun `the owner sees the invitations of the place`() {
            val owner = data.user()
            val place = data.place(owner)
            val plan = data.eventPlan(title = "Opening")
            data.placeInvitation(plan, place)

            mockMvc.get("/api/places/${place.id}/invitations") { with(tokenFor(owner.sub, Roles.PLACE_MANAGER)) }
                .andExpect {
                    status { isOk() }
                    jsonPath("$", hasSize<Any>(1))
                    jsonPath("$[0].eventPlanId") { value(plan.id.toString()) }
                    jsonPath("$[0].title") { value("Opening") }
                    jsonPath("$[0].state") { value("PENDING") }
                    jsonPath("$[0].startDateTime") { exists() }
                }
        }

        @Test
        fun `another place manager may not see the invitations`() {
            val place = data.place()
            data.placeInvitation(data.eventPlan(), place)

            mockMvc.get("/api/places/${place.id}/invitations") {
                with(tokenFor(UUID.randomUUID(), Roles.PLACE_MANAGER))
            }.andExpect { status { isForbidden() } }
        }

        @Test
        fun `invitations of an unknown place are not found`() {
            mockMvc.get("/api/places/${UUID.randomUUID()}/invitations") {
                with(tokenFor(UUID.randomUUID(), Roles.PLACE_MANAGER))
            }.andExpect { status { isNotFound() } }
        }

        @Test
        fun `the owner accepts and then rejects an invitation`() {
            val owner = data.user()
            val place = data.place(owner)
            val plan = data.eventPlan()
            data.placeInvitation(plan, place)
            val url = "/api/places/${place.id}/invitations/${plan.id}/respond"

            mockMvc.put("$url?state=accept") { with(tokenFor(owner.sub, Roles.PLACE_MANAGER)) }
                .andExpect { status { isNoContent() } }
            assertEquals(EventPlanPlaceInvitationState.ACCEPTED.name, invitationState())

            mockMvc.put("$url?state=reject") { with(tokenFor(owner.sub, Roles.PLACE_MANAGER)) }
                .andExpect { status { isNoContent() } }
            assertEquals(EventPlanPlaceInvitationState.REJECTED.name, invitationState())
        }

        @Test
        fun `an unknown answer is a bad request`() {
            val owner = data.user()
            val place = data.place(owner)
            val plan = data.eventPlan()
            data.placeInvitation(plan, place)

            mockMvc.put("/api/places/${place.id}/invitations/${plan.id}/respond?state=maybe") {
                with(tokenFor(owner.sub, Roles.PLACE_MANAGER))
            }.andExpect { status { isBadRequest() } }
            assertEquals(EventPlanPlaceInvitationState.PENDING.name, invitationState())
        }

        @Test
        fun `another place manager may not answer`() {
            val place = data.place()
            val plan = data.eventPlan()
            data.placeInvitation(plan, place)

            mockMvc.put("/api/places/${place.id}/invitations/${plan.id}/respond?state=accept") {
                with(tokenFor(UUID.randomUUID(), Roles.PLACE_MANAGER))
            }.andExpect { status { isForbidden() } }
            assertEquals(EventPlanPlaceInvitationState.PENDING.name, invitationState())
        }

        @Test
        fun `the owner of one place cannot answer an invitation sent to another place`() {
            val owner = data.user()
            val mine = data.place(owner)
            val plan = data.eventPlan()
            data.placeInvitation(plan, data.place(name = "Invited"))

            mockMvc.put("/api/places/${mine.id}/invitations/${plan.id}/respond?state=accept") {
                with(tokenFor(owner.sub, Roles.PLACE_MANAGER))
            }.andExpect { status { isNotFound() } }
            assertEquals(EventPlanPlaceInvitationState.PENDING.name, invitationState())
        }

        @Test
        fun `answering for an unknown place is not found`() {
            mockMvc.put("/api/places/${UUID.randomUUID()}/invitations/${UUID.randomUUID()}/respond?state=accept") {
                with(tokenFor(UUID.randomUUID(), Roles.PLACE_MANAGER))
            }.andExpect { status { isNotFound() } }
        }

        private fun invitationState(): String =
            jdbc.queryForObject("SELECT state FROM event_plan_place_invitation_entity", String::class.java)!!
    }
}
