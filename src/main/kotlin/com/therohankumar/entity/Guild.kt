package com.therohankumar.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
@Table(name = "guilds")
class Guild(
    @Id
    val id: Long,

    @Column(length = 100)
    var name: String? = null,

    @Column(name = "joined_at", nullable = false)
    val joinedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "left_at")
    var leftAt: OffsetDateTime? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prefix_prefs", columnDefinition = "jsonb")
    val prefixPrefs: String = "{}"
)
