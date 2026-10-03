package com.example.model

data class ActiveReadingSession(
    val id: String,
    val bookId: String,
    val bookTitle: String,
    val startTime: Long,
    val lastResumeTime: Long,
    val accumulatedSeconds: Long,
    val isRunning: Boolean,
    val chaptersCovered: String = "",
    val pagesCovered: String = "",
    val notes: String = ""
) {
    fun calculateElapsedSeconds(now: Long = System.currentTimeMillis()): Long {
        return if (isRunning && lastResumeTime > 0L) {
            val delta = maxOf(0L, (now - lastResumeTime) / 1000L)
            accumulatedSeconds + delta
        } else {
            accumulatedSeconds
        }
    }

    fun getFormattedTime(now: Long = System.currentTimeMillis()): String {
        val totalSec = calculateElapsedSeconds(now)
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
