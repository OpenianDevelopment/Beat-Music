package com.therohankumar.repository

import com.therohankumar.entity.PlayEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PlayEventRepository : JpaRepository<PlayEvent, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE PlayEvent e SET e.skippedAtMs = :posMs WHERE e.id = :id")
    fun updateSkippedAt(id: Long, posMs: Int)

    /** Returns track IDs of the last [limit] played tracks in a guild (most recent first). */
    @Query("""
        SELECT e.trackId FROM PlayEvent e
        WHERE e.guildId = :guildId
        ORDER BY e.playedAt DESC
    """)
    fun findRecentTrackIds(guildId: Long, limit: org.springframework.data.domain.Pageable): List<String>

    /** Top tracks in a guild: [track_id, track_title, track_author, count]. */
    @Query(value = """
        SELECT track_id, track_title, track_author, COUNT(*) AS play_count
        FROM play_events
        WHERE guild_id = :guildId
        GROUP BY track_id, track_title, track_author
        ORDER BY play_count DESC
        LIMIT :limit
    """, nativeQuery = true)
    fun topTracksByGuild(guildId: Long, limit: Int): List<Array<Any>>

    /** Top users in a guild: [user_id, count]. */
    @Query(value = """
        SELECT user_id, COUNT(*) AS play_count
        FROM play_events
        WHERE guild_id = :guildId AND user_id <> 0
        GROUP BY user_id
        ORDER BY play_count DESC
        LIMIT :limit
    """, nativeQuery = true)
    fun topUsersByGuild(guildId: Long, limit: Int): List<Array<Any>>

    /** Daily play counts for a guild over the last [days] days: [date, count]. */
    @Query(value = """
        SELECT DATE(played_at) AS day, COUNT(*) AS count
        FROM play_events
        WHERE guild_id = :guildId
          AND played_at > NOW() - INTERVAL '1 day' * :days
        GROUP BY day
        ORDER BY day
    """, nativeQuery = true)
    fun dailyPlaysByGuild(guildId: Long, days: Int): List<Array<Any>>

    /** Total autoplay-triggered events (user_id = 0). */
    fun countByUserId(userId: Long): Long

    /**
     * Co-play query: finds tracks frequently played after any of [seedTrackIds] in the same guild,
     * excluding the seeds themselves. Returns (trackId, score) ordered by score desc.
     */
    @Query(value = """
        SELECT pe2.track_id, COUNT(*) AS score
        FROM play_events pe1
        JOIN play_events pe2
          ON  pe2.guild_id  = pe1.guild_id
          AND pe2.played_at > pe1.played_at
          AND pe2.played_at < pe1.played_at + INTERVAL '3 hours'
        WHERE pe1.guild_id  = :guildId
          AND pe1.track_id  IN :seedTrackIds
          AND pe2.track_id NOT IN :seedTrackIds
        GROUP BY pe2.track_id
        ORDER BY score DESC
        LIMIT 5
    """, nativeQuery = true)
    fun findCoPlayCandidates(guildId: Long, seedTrackIds: Collection<String>): List<Array<Any>>
}
