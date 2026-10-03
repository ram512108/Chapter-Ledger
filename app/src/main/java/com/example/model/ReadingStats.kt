package com.example.model

data class PaceEstimate(
    val bookId: String,
    val bookTitle: String,
    val remainingChapters: Int,
    val estimatedDaysToFinish: Int,
    val estimatedFinishDateMillis: Long
)

data class HeatmapDay(
    val dateMillis: Long,
    val dayOfMonth: Int,
    val monthOfYear: Int,
    val year: Int,
    val chaptersCount: Int,
    val readingMinutes: Int,
    val level: Int // 0 (none), 1 (light), 2 (medium), 3 (high), 4 (intense)
)

data class ReadingStats(
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val totalChaptersRead: Int = 0,
    val totalBooksFinished: Int = 0,
    val totalPagesRead: Int = 0,
    val totalReadingMinutes: Int = 0,
    val chaptersThisWeek: Int = 0,
    val chaptersThisMonth: Int = 0,
    val chaptersThisYear: Int = 0,
    val pagesThisYear: Int = 0,
    val booksThisYear: Int = 0,
    val paceEstimates: List<PaceEstimate> = emptyList(),
    val heatmapDays: List<HeatmapDay> = emptyList(),
    val genreDistribution: Map<String, Int> = emptyMap(),
    val formatDistribution: Map<BookFormat, Int> = emptyMap()
)
