package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.ReadingSession

@Entity(
    tableName = "reading_sessions",
    indices = [Index(value = ["bookId"])]
)
data class ReadingSessionEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val bookTitle: String,
    val chapterNumbersCovered: String, // comma-separated ints
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val pagesCovered: Int,
    val notes: String
) {
    fun toDomain(): ReadingSession {
        return ReadingSession(
            id = id,
            bookId = bookId,
            bookTitle = bookTitle,
            chapterNumbersCovered = if (chapterNumbersCovered.isBlank()) emptyList() else chapterNumbersCovered.split(",").mapNotNull { it.trim().toIntOrNull() },
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            pagesCovered = pagesCovered,
            notes = notes
        )
    }

    companion object {
        fun fromDomain(session: ReadingSession): ReadingSessionEntity {
            return ReadingSessionEntity(
                id = session.id,
                bookId = session.bookId,
                bookTitle = session.bookTitle,
                chapterNumbersCovered = session.chapterNumbersCovered.joinToString(","),
                startTime = session.startTime,
                endTime = session.endTime,
                durationMinutes = session.durationMinutes,
                pagesCovered = session.pagesCovered,
                notes = session.notes
            )
        }
    }
}
