package com.partymap.backend.domain.user.db

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class UserEntity(
    @Id
    @Column(updatable = false)
    var sub: UUID,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdDate: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedDate: Instant? = null,
)
