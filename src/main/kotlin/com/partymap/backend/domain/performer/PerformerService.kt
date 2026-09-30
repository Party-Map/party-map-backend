package com.partymap.backend.domain.performer

import com.partymap.backend.domain.common.exception.NotFoundException
import com.partymap.backend.domain.common.exception.requireOwner
import com.partymap.backend.domain.performer.db.PerformerEntity
import com.partymap.backend.domain.performer.db.PerformerRepository
import com.partymap.backend.domain.performer.dto.PerformerAdminListItemDto
import com.partymap.backend.domain.performer.dto.PerformerCreateDto
import com.partymap.backend.domain.performer.dto.PerformerDto
import com.partymap.backend.domain.user.UserRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class PerformerService(
    private val performerRepository: PerformerRepository,
    private val userRepository: UserRepository,
) {
    fun list(): List<PerformerDto> = performerRepository.findAll(Sort.by("name")).map { it.toDto() }

    fun get(id: UUID): PerformerDto = find(id).toDto()

    fun liked(sub: UUID): List<PerformerDto> = performerRepository.findAllByLikedByUsersSub(sub).map { it.toDto() }

    fun owned(sub: UUID): List<PerformerAdminListItemDto> =
        performerRepository.findAllByOwnerSub(sub).map { it.toAdminListItemDto() }

    @Transactional
    fun create(sub: UUID, dto: PerformerCreateDto): PerformerDto =
        performerRepository.save(dto.toEntity(userRepository.getReferenceById(sub))).toDto()

    @Transactional
    fun update(sub: UUID, id: UUID, dto: PerformerCreateDto): PerformerDto {
        val performer = find(id)
        requireOwner(performer.owner.sub, sub, "edit this performer")
        performer.updateFromDto(dto)
        return performerRepository.saveAndFlush(performer).toDto()
    }

    private fun find(id: UUID): PerformerEntity =
        performerRepository.findById(id).orElseThrow { NotFoundException("Performer", id) }
}
