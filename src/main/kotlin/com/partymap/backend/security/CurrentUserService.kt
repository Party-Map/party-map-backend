package com.partymap.backend.security

import com.partymap.backend.domain.user.db.UserEntity
import com.partymap.backend.domain.user.db.UserRepository
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CurrentUserService(
    private val userRepository: UserRepository,
) {

    fun getOrCreateUser(jwt: Jwt): UserEntity {
        val sub = UUID.fromString(jwt.subject)

        return userRepository.findBySub(sub)
            ?: userRepository.save(
                UserEntity(
                    sub = sub,
                )
            )
    }
}
