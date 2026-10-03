package com.example.model

import java.util.UUID

data class ReadingSession(
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val bookTitle: String = "",
    val chapterNumbersCovered: List<Int> = emptyList(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 15,
    val pagesCovered: Int = 0,
    val notes: String = ""
)
