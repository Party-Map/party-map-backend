package com.partymap.backend.domain.event

import com.partymap.backend.config.Roles
import com.partymap.backend.domain.event.dto.EventAdminListItemDto
import com.partymap.backend.domain.event.dto.EventDto
import com.partymap.backend.domain.event.dto.PlaceUpcomingEventDto
import com.partymap.backend.domain.like.dto.LikedEventsGroupedDto
import com.partymap.backend.domain.place.dto.PlaceDto
import com.partymap.backend.domain.user.CurrentUserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/events")
class EventController(private val eventService: EventService, private val currentUserService: CurrentUserService) {
    @GetMapping
    fun getEvents(
        @RequestParam(required = false) placeId: UUID?,
        @RequestParam(required = false) performerId: UUID?,
    ): List<EventDto> = eventService.list(placeId, performerId)

    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: UUID): EventDto = eventService.get(id)

    @GetMapping("/{id}/place")
    fun getEventPlace(@PathVariable id: UUID): PlaceDto = eventService.placeOf(id)

    @GetMapping("/upcoming-events")
    fun getUpcomingEvents(): List<PlaceUpcomingEventDto> = eventService.upcomingPerPlace()

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/liked-events")
    fun getLikedEvents(@AuthenticationPrincipal jwt: Jwt): LikedEventsGroupedDto =
        eventService.likedGrouped(currentUserService.getOrCreateUser(jwt).sub)

    @PreAuthorize("hasRole('${Roles.EVENT_ORGANIZER}')")
    @GetMapping("/owned-events")
    fun getOwnedEvents(@AuthenticationPrincipal jwt: Jwt): List<EventAdminListItemDto> =
        eventService.owned(currentUserService.getOrCreateUser(jwt).sub)
}
