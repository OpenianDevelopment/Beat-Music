package com.therohankumar.repository

import com.therohankumar.entity.CommandLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface CommandLogRepository : JpaRepository<CommandLog, Long> {

    /** Commands per hour for the last 24 hours. Returns [hour_truncated, count]. */
    @Query(value = """
        SELECT DATE_TRUNC('hour', executed_at) AS hour, COUNT(*) AS count
        FROM command_logs
        WHERE executed_at > NOW() - INTERVAL '24 hours'
        GROUP BY hour
        ORDER BY hour
    """, nativeQuery = true)
    fun commandsPerHourLast24h(): List<Array<Any>>

    /** Per-command stats in a date range: [command, total, errors]. */
    @Query(value = """
        SELECT command,
               COUNT(*)                            AS total,
               SUM(CASE WHEN NOT success THEN 1 ELSE 0 END) AS errors
        FROM command_logs
        WHERE executed_at BETWEEN :from AND :to
        GROUP BY command
        ORDER BY total DESC
    """, nativeQuery = true)
    fun commandStats(from: OffsetDateTime, to: OffsetDateTime): List<Array<Any>>
}
