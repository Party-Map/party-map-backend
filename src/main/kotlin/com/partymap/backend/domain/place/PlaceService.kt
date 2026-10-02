package com.partymap.backend.domain.place

import com.partymap.backend.domain.common.exception.InvalidRequestException
import com.partymap.backend.domain.common.exception.NotFoundException
import com.partymap.backend.domain.common.exception.requireOwner
import com.partymap.backend.domain.common.geo.Hungary
import com.partymap.backend.domain.place.db.PlaceEntity
import com.partymap.backend.domain.place.db.PlaceRepository
import com.partymap.backend.domain.place.dto.PlaceAdminListItemDto
import com.partymap.backend.domain.place.dto.PlaceCreateDto
import com.partymap.backend.domain.place.dto.PlaceDto
import com.partymap.backend.domain.user.UserRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class PlaceService(private val placeRepository: PlaceRepository, private val userRepository: UserRepository) {
    /** Every place, or only those inside [bounds]. */
    fun list(bounds: BoundingBox?): List<PlaceDto> {
        val places = if (bounds == null) {
            placeRepository.findAll(Sort.by("name"))
        } else {
            placeRepository.findAllByLocationLatitudeBetweenAndLocationLongitudeBetween(
                bounds.minLatitude,
                bounds.maxLatitude,
                bounds.minLongitude,
                bounds.maxLongitude,
            )
        }
        return places.map { it.toDto() }
    }

    fun get(id: UUID): PlaceDto = find(id).toDto()

    fun liked(sub: UUID): List<PlaceDto> = placeRepository.findAllByLikedByUsersSub(sub).map { it.toDto() }

    fun owned(sub: UUID): List<PlaceAdminListItemDto> =
        placeRepository.findAllByOwnerSub(sub).map { it.toAdminListItemDto() }

    /** Every place, in the short form the organizer picks a venue from. */
    fun adminList(): List<PlaceAdminListItemDto> = placeRepository.findAll(
        Sort.by("name"),
    ).map { it.toAdminListItemDto() }

    @Transactional
    fun create(sub: UUID, dto: PlaceCreateDto): PlaceDto {
        requireInHungary(dto)
        return placeRepository.save(dto.toEntity(userRepository.getReferenceById(sub))).toDto()
    }

    @Transactional
    fun update(sub: UUID, id: UUID, dto: PlaceCreateDto): PlaceDto {
        val place = find(id)
        requireOwner(place.owner.sub, sub, "edit this place")
        requireInHungary(dto)
        place.updateFromDto(dto)
        return placeRepository.saveAndFlush(place).toDto()
    }

    /** The map covers Hungary only: a place (and so every event, which happens at a place) must lie inside it. */
    private fun requireInHungary(dto: PlaceCreateDto) {
        if (!Hungary.contains(dto.location.latitude, dto.location.longitude)) {
            throw InvalidRequestException("The place must be inside Hungary")
        }
    }

    private fun find(id: UUID): PlaceEntity = placeRepository.findById(
        id,
    ).orElseThrow { NotFoundException("Place", id) }
}
