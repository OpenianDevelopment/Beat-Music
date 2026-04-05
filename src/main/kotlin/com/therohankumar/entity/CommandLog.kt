package com.therohankumar.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
@Table(name = "command_logs")
class CommandLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "guild_id")
    val guildId: Long? = null,

    @Column(name = "user_id")
    val userId: Long? = null,

    @Column(name = "command", length = 100, nullable = false)
    val command: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "args", columnDefinition = "jsonb")
    val args: String? = null,

    @Column(name = "executed_at", nullable = false)
    val executedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "success", nullable = false)
    val success: Boolean,

    @Column(name = "error_msg", columnDefinition = "text")
    val errorMsg: String? = null
)
