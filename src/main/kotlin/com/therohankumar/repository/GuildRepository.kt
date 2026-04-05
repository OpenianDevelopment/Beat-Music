package com.therohankumar.repository

import com.therohankumar.entity.Guild
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface GuildRepository : JpaRepository<Guild, Long> {

    fun countByIsActiveTrue(): Long

    fun countByJoinedAtAfter(since: OffsetDateTime): Long

    @Query("""
        SELECT g FROM Guild g
        WHERE (:q = '' OR LOWER(COALESCE(g.name,'')) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY g.joinedAt DESC
    """)
    fun search(q: String, pageable: Pageable): Page<Guild>
}
