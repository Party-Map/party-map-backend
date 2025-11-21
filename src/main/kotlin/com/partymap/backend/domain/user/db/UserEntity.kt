package com.partymap.backend.domain.user.db

import com.partymap.backend.domain.common.db.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(

    @Column(nullable = false, unique = true)
    var sub: UUID,
) : BaseEntity()
