package com.partymap.backend.domain.user

import jakarta.transaction.Transactional
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.*

@Service
class CurrentUserService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun getOrCreateUser(jwt: Jwt): UserEntity {
        val sub = UUID.fromString(jwt.subject)

        return userRepository.findById(sub).orElseGet {
            userRepository.save(
                UserEntity(
                    sub = sub,
                )
            )
        }
    }
}