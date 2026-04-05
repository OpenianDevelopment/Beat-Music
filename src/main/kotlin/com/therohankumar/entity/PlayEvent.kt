package com.therohankumar.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
@Table(name = "play_events")
class PlayEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "guild_id")
    val guildId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "track_id", nullable = false, length = 255)
    val trackId: String,

    @Column(name = "track_title", length = 500)
    val trackTitle: String? = null,

    @Column(name = "track_author", length = 255)
    val trackAuthor: String? = null,

    @Column(name = "track_duration")
    val trackDuration: Int? = null,

    @Column(name = "source", length = 50)
    val source: String? = null,

    @Column(name = "played_at", nullable = false)
    val playedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "skipped_at_ms")
    var skippedAtMs: Int? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filter_config", columnDefinition = "jsonb")
    val filterConfig: String? = null,

    @Column(name = "requested_by")
    val requestedBy: Long? = null
)
