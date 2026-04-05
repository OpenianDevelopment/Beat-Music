package com.therohankumar.repository

import com.therohankumar.entity.Track
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TrackRepository : JpaRepository<Track, String> {

    @Modifying
    @Query("UPDATE Track t SET t.playCount = t.playCount + 1 WHERE t.trackId = :trackId")
    fun incrementPlayCount(trackId: String)

    @Query("SELECT t FROM Track t ORDER BY t.playCount DESC")
    fun findTopByPlayCount(pageable: org.springframework.data.domain.Pageable): List<Track>
}
