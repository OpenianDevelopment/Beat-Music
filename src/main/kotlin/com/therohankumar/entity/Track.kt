package com.therohankumar.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "tracks")
class Track(
    @Id
    @Column(name = "track_id", length = 255)
    val trackId: String,

    @Column(length = 500)
    val title: String? = null,

    @Column(length = 255)
    val author: String? = null,

    @Column(name = "duration_ms")
    val durationMs: Int? = null,

    @Column(length = 512)
    val thumbnail: String? = null,

    @Column(length = 50)
    val source: String? = null,

    @Column(name = "first_seen", nullable = false)
    val firstSeen: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "play_count", nullable = false)
    var playCount: Long = 0
)
