package com.partymap.backend.domain.user

import com.partymap.backend.domain.common.exception.UnauthorizedException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/** The backend stores only the Keycloak subject of each user; the row is created on the user's first request. */
@Service
class CurrentUserService(private val userRepository: UserRepository) {
    @Transactional
    fun getOrCreateUser(jwt: Jwt): UserEntity {
        val sub = subjectOf(jwt)
        return userRepository.findById(sub).orElseGet {
            userRepository.insertIfAbsent(sub)
            userRepository.findById(sub).orElseThrow()
        }
    }

    private fun subjectOf(jwt: Jwt): UUID = try {
        UUID.fromString(jwt.subject)
    } catch (_: IllegalArgumentException) {
        throw UnauthorizedException("The token's subject is not a user id.")
    }
}
