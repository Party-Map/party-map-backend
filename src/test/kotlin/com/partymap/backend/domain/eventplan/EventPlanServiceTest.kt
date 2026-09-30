package com.partymap.backend.domain.eventplan

import com.partymap.backend.TestcontainersConfig
import com.partymap.backend.domain.common.db.GeoPointEmbeddable
import com.partymap.backend.domain.event.db.EventRepository
import com.partymap.backend.domain.event.db.EventType
import com.partymap.backend.domain.eventplan.db.EventPlanEntity
import com.partymap.backend.domain.eventplan.db.EventPlanLineupInvitationState
import com.partymap.backend.domain.eventplan.db.EventPlanPlaceInvitationState
import com.partymap.backend.domain.eventplan.db.EventPlanRepository
import com.partymap.backend.domain.eventplan.exception.AlreadyInvitedPerformerException
import com.partymap.backend.domain.eventplan.exception.InvalidStartOrEndTimeException
import com.partymap.backend.domain.eventplan.exception.NoValidPlaceInvitationException
import com.partymap.backend.domain.eventplan.exception.PendingLineupInvitationException
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.user.UserEntity
import com.partymap.backend.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfig::class)
class EventPlanServiceTest {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var performerRepository: PerformerRepository

    @Autowired
    lateinit var eventPlanRepository: EventPlanRepository

    @Autowired
    lateinit var eventPlanService: EventPlanService

    @Autowired
    lateinit var placeRepository: PlaceRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Transactional
    fun createTestEventPlan(testOwner: UserEntity): EventPlanEntity = eventPlanRepository.save(
        EventPlanEntity(
            owner = testOwner,
            title = "Test Event Plan",
            description = "This is a test event plan",
            startDateTime = LocalDateTime.now(),
            endDateTime = LocalDateTime.now().plusHours(2),
            image = null,
            price = "one million dollars",
            kind = EventType.PUB,
        ),
    )

    @Transactional
    fun createTestPerformer(testPerformerOwner: UserEntity): PerformerEntity = performerRepository.save(
        PerformerEntity(
            owner = testPerformerOwner,
            name = "Test Performer",
            genre = "Rock",
            bio = "This is a test performer",
        ),
    )

    @Transactional
    fun createUser(): UserEntity {
        val testOwner = userRepository.saveAndFlush(
            UserEntity(
                sub = UUID.randomUUID(),
            ),
        )

        return testOwner
    }

    @Transactional
    fun cratePlace(owner: UserEntity): PlaceEntity {
        val place = placeRepository.saveAndFlush(
            PlaceEntity(
                name = "Test Place",
                location = GeoPointEmbeddable(
                    latitude = 40.7128,
                    longitude = -74.0060,
                ),
                address = "123 Test St, Test City, TC 12345",
                city = "Test City",
                description = "This is a test place",
                owner = owner,
            ),
        )
        return place
    }

    @Test
    @Transactional
    fun itShouldInvitePerformer() {
        val testOwner = createUser()
        val testPerformerOwner = createUser()
        val testPerformer = createTestPerformer(testPerformerOwner)
        val testEventPlan = createTestEventPlan(testOwner)

        eventPlanService.invitePerformer(
            testEventPlan,
            testPerformer,
            testEventPlan.startDateTime,
            testEventPlan.startDateTime.plusHours(2),
        )

        val invitation = testEventPlan.lineupInvitations.find { it.id.performer.id == testPerformer.id }
        // Assert that the invitation was created
        Assertions.assertNotNull(invitation)
    }

    @Test
    @Transactional
    fun itShouldThrowPerformerAlreadyInvited() {
        val testOwner = createUser()
        val testPerformerOwner = createUser()
        val testPerformer = createTestPerformer(testPerformerOwner)
        val testEventPlan = createTestEventPlan(testOwner)

        eventPlanService.invitePerformer(
            testEventPlan,
            testPerformer,
            testEventPlan.startDateTime,
            testEventPlan.startDateTime.plusMinutes(30),
        )

        assertThrows<AlreadyInvitedPerformerException> {
            eventPlanService.invitePerformer(
                testEventPlan,
                testPerformer,
                testEventPlan.startDateTime.plusMinutes(30),
                testEventPlan.endDateTime.plusMinutes(60),
            )
        }
    }

    @Test
    @Transactional
    fun itShouldThrowInvalidStartOrEndDate() {
        val testOwner = createUser()
        val testPerformerOwner = createUser()
        val testPerformer = createTestPerformer(testPerformerOwner)
        val testEventPlan = createTestEventPlan(testOwner)

        assertThrows<InvalidStartOrEndTimeException> {
            eventPlanService.invitePerformer(
                testEventPlan,
                testPerformer,
                testEventPlan.startDateTime.plusHours(1),
                testEventPlan.startDateTime,
            )
        }
    }

    @Test
    @Transactional
    fun itShouldInviteAPlace() {
        val testPlaceOwner = createUser()
        val testPlace = cratePlace(testPlaceOwner)

        val testEventOwner = createUser()
        val testEventPlan = createTestEventPlan(testEventOwner)

        eventPlanService.invitePlace(
            testEventPlan,
            testPlace,
        )

        val invitation = testEventPlan.placeInvitations.find { it.id.place.id == testPlace.id }
        // Assert that the invitation was created
        Assertions.assertNotNull(invitation)
    }

    @Test
    @Transactional
    fun itShouldReplaceAPlaceInvitation() {
        val testPlace1Owner = createUser()
        val testPlace1 = cratePlace(testPlace1Owner)

        val testPlace2Owner = createUser()
        val testPlace2 = cratePlace(testPlace2Owner)

        val testEventOwner = createUser()
        val testEventPlan = createTestEventPlan(testEventOwner)

        eventPlanService.invitePlace(
            testEventPlan,
            testPlace1,
        )

        val invitation = testEventPlan.placeInvitations.find { it.id.place.id == testPlace1.id }
        // Assert that the invitation was created
        Assertions.assertNotNull(invitation)

        eventPlanService.invitePlace(
            testEventPlan,
            testPlace2,
        )

        val invitation2 = testEventPlan.placeInvitations.find { it.id.place.id == testPlace2.id }
        // Assert that the new invitation was created
        Assertions.assertNotNull(invitation2)
    }

    @Test
    @Transactional
    fun itShouldPublishValidEventPlan() {
        val owner = createUser()

        val performerOwner1 = createUser()
        val performer1 = createTestPerformer(performerOwner1)

        val performerOwner2 = createUser()
        val performer2 = createTestPerformer(performerOwner2)

        val performerOwner3 = createUser()
        val performer3 = createTestPerformer(performerOwner3)

        val placeOwner = createUser()
        val place = cratePlace(placeOwner)

        val start = LocalDateTime.of(2025, 1, 1, 20, 0)
        val end = LocalDateTime.of(2025, 1, 1, 22, 0)

        val eventPlan = eventPlanRepository.save(
            EventPlanEntity(
                owner = owner,
                title = "Publishable Event Plan",
                description = "Description",
                startDateTime = start,
                endDateTime = end,
                price = "small loan of a million dollars",
                kind = EventType.PUB,
                image = null,
            ),
        )

        // Invite three performers
        eventPlanService.invitePerformer(
            eventPlan,
            performer1,
            start,
            start.plusMinutes(30),
        )
        eventPlanService.invitePerformer(
            eventPlan,
            performer2,
            start.plusMinutes(30),
            start.plusMinutes(60),
        )
        eventPlanService.invitePerformer(
            eventPlan,
            performer3,
            start.plusMinutes(60),
            start.plusMinutes(90),
        )

        // Set lineup invitation states: two accepted, one rejected
        val invitation1 = eventPlan.lineupInvitations.first { it.id.performer.id == performer1.id }
        val invitation2 = eventPlan.lineupInvitations.first { it.id.performer.id == performer2.id }
        val invitation3 = eventPlan.lineupInvitations.first { it.id.performer.id == performer3.id }

        invitation1.state = EventPlanLineupInvitationState.ACCEPTED
        invitation2.state = EventPlanLineupInvitationState.ACCEPTED
        invitation3.state = EventPlanLineupInvitationState.REJECTED

        // Invite and accept place
        eventPlanService.invitePlace(eventPlan, place)
        val placeInvitation = eventPlan.placeInvitations.first()
        placeInvitation.state = EventPlanPlaceInvitationState.ACCEPTED

        // Publish
        eventPlanService.publish(owner, eventPlan)

        val events = eventRepository.findAllByOwnerSub(owner.sub)
        Assertions.assertEquals(1, events.size)
        val event = events.first()

        Assertions.assertEquals(eventPlan.title, event.title)
        Assertions.assertEquals(eventPlan.description, event.description)
        Assertions.assertEquals(start, event.start)
        Assertions.assertEquals(end, event.end)
        Assertions.assertEquals(eventPlan.image, event.image)
        Assertions.assertEquals(eventPlan.price, event.price)
        Assertions.assertEquals(eventPlan.kind, event.kind)
        Assertions.assertEquals(place.id, event.place.id)
        Assertions.assertEquals(owner.sub, event.owner.sub)

        // Lineup items mirror accepted invitations (2 accepted)
        Assertions.assertEquals(2, event.lineupItems.size)
        val lineupPerformerIds = event.lineupItems.map { it.id.performer.id }.toSet()
        Assertions.assertTrue(lineupPerformerIds.contains(performer1.id))
        Assertions.assertTrue(lineupPerformerIds.contains(performer2.id))
        Assertions.assertFalse(lineupPerformerIds.contains(performer3.id))

        // Event plan should be deleted
        val stillExists = eventPlanRepository.findById(eventPlan.id!!).isPresent
        Assertions.assertFalse(stillExists)
    }

    @Test
    @Transactional
    fun itShouldThrowExceptionIfThereIsAPendingLineupInvitation() {
        val owner = createUser()

        val performerOwner1 = createUser()
        val performer1 = createTestPerformer(performerOwner1)

        val performerOwner2 = createUser()
        val performer2 = createTestPerformer(performerOwner2)

        val performerOwner3 = createUser()
        val performer3 = createTestPerformer(performerOwner3)

        val placeOwner = createUser()
        val place = cratePlace(placeOwner)

        val start = LocalDateTime.of(2025, 1, 1, 20, 0)
        val end = LocalDateTime.of(2025, 1, 1, 22, 0)

        val eventPlan = eventPlanRepository.save(
            EventPlanEntity(
                owner = owner,
                title = "Pending Lineup Event Plan",
                description = "Description",
                startDateTime = start,
                endDateTime = end,
                price = "small loan of a million dollars",
                kind = EventType.PUB,
                image = null,
            ),
        )

        // Invite three performers, then explicitly set all possible states
        eventPlanService.invitePerformer(
            eventPlan,
            performer1,
            start,
            start.plusMinutes(30),
        )
        eventPlanService.invitePerformer(
            eventPlan,
            performer2,
            start.plusMinutes(30),
            start.plusMinutes(60),
        )
        eventPlanService.invitePerformer(
            eventPlan,
            performer3,
            start.plusMinutes(60),
            start.plusMinutes(90),
        )

        val invitation1 = eventPlan.lineupInvitations.first { it.id.performer.id == performer1.id }
        val invitation2 = eventPlan.lineupInvitations.first { it.id.performer.id == performer2.id }
        eventPlan.lineupInvitations.first { it.id.performer.id == performer3.id }

        // Cover all states:
        invitation1.state = EventPlanLineupInvitationState.ACCEPTED
        invitation2.state = EventPlanLineupInvitationState.REJECTED
        // invitation3 remains PENDING by default to trigger the exception

        // Valid place (accepted)
        eventPlanService.invitePlace(eventPlan, place)
        val placeInvitation = eventPlan.placeInvitations.first()
        placeInvitation.state = EventPlanPlaceInvitationState.ACCEPTED

        assertThrows<PendingLineupInvitationException> {
            eventPlanService.publish(owner, eventPlan)
        }
    }

    @Test
    @Transactional
    fun itShouldThrowIfNoValidPlaceInvitationOnPublish() {
        val owner = createUser()

        val performerOwner1 = createUser()
        val performer1 = createTestPerformer(performerOwner1)

        val performerOwner2 = createUser()
        val performer2 = createTestPerformer(performerOwner2)

        val performerOwner3 = createUser()
        val performer3 = createTestPerformer(performerOwner3)

        val start = LocalDateTime.of(2025, 1, 1, 20, 0)
        val end = LocalDateTime.of(2025, 1, 1, 22, 0)

        val eventPlan = eventPlanRepository.save(
            EventPlanEntity(
                owner = owner,
                title = "No Place Invitation Event Plan",
                description = "Description",
                startDateTime = start,
                endDateTime = end,
                price = "small loan of a million dollars",
                kind = EventType.PUB,
                image = null,
            ),
        )

        // Invite three performers
        eventPlanService.invitePerformer(
            eventPlan,
            performer1,
            start,
            start.plusMinutes(30),
        )
        eventPlanService.invitePerformer(
            eventPlan,
            performer2,
            start.plusMinutes(30),
            start.plusMinutes(60),
        )
        eventPlanService.invitePerformer(
            eventPlan,
            performer3,
            start.plusMinutes(60),
            start.plusMinutes(90),
        )

        // Set lineup invitation states: all non‑pending (so the only problem is missing place)
        val invitation1 = eventPlan.lineupInvitations.first { it.id.performer.id == performer1.id }
        val invitation2 = eventPlan.lineupInvitations.first { it.id.performer.id == performer2.id }
        val invitation3 = eventPlan.lineupInvitations.first { it.id.performer.id == performer3.id }

        invitation1.state = EventPlanLineupInvitationState.ACCEPTED
        invitation2.state = EventPlanLineupInvitationState.ACCEPTED
        invitation3.state = EventPlanLineupInvitationState.REJECTED

        // Do NOT invite any place -> NoValidPlaceInvitationException expected
        assertThrows<NoValidPlaceInvitationException> {
            eventPlanService.publish(owner, eventPlan)
        }
    }

    @Test
    @Transactional
    fun itShouldThrowIfPlaceInvitationIsRejectedOnPublish() {
        val owner = createUser()

        val performerOwner1 = createUser()
        val performer1 = createTestPerformer(performerOwner1)

        val performerOwner2 = createUser()
        val performer2 = createTestPerformer(performerOwner2)

        val performerOwner3 = createUser()
        val performer3 = createTestPerformer(performerOwner3)

        val placeOwner = createUser()
        val place = cratePlace(placeOwner)

        val start = LocalDateTime.of(2025, 1, 1, 20, 0)
        val end = LocalDateTime.of(2025, 1, 1, 22, 0)

        val eventPlan = eventPlanRepository.save(
            EventPlanEntity(
                owner = owner,
                title = "Rejected Place Invitation Event Plan",
                description = "Description",
                startDateTime = start,
                endDateTime = end,
                price = "small loan of a million dollars",
                kind = EventType.PUB,
                image = null,
            ),
        )

        // Invite three performers
        eventPlanService.invitePerformer(
            eventPlan,
            performer1,
            start,
            start.plusMinutes(30),
        )
        eventPlanService.invitePerformer(
            eventPlan,
            performer2,
            start.plusMinutes(30),
            start.plusMinutes(60),
        )
        eventPlanService.invitePerformer(
            eventPlan,
            performer3,
            start.plusMinutes(60),
            start.plusMinutes(90),
        )

        // All lineup invitations non‑pending (so only the place is invalid)
        val invitation1 = eventPlan.lineupInvitations.first { it.id.performer.id == performer1.id }
        val invitation2 = eventPlan.lineupInvitations.first { it.id.performer.id == performer2.id }
        val invitation3 = eventPlan.lineupInvitations.first { it.id.performer.id == performer3.id }

        invitation1.state = EventPlanLineupInvitationState.ACCEPTED
        invitation2.state = EventPlanLineupInvitationState.ACCEPTED
        invitation3.state = EventPlanLineupInvitationState.REJECTED

        // Invite place but mark it as REJECTED
        eventPlanService.invitePlace(eventPlan, place)
        val placeInvitation = eventPlan.placeInvitations.first()
        placeInvitation.state = EventPlanPlaceInvitationState.REJECTED

        // No place with ACCEPTED state -> should throw NoValidPlaceInvitationException
        assertThrows<NoValidPlaceInvitationException> {
            eventPlanService.publish(owner, eventPlan)
        }
    }
}
