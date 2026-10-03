package com.example.model

import java.util.UUID

enum class GoalType(val displayName: String) {
    BOOKS("Books"),
    CHAPTERS("Chapters"),
    PAGES("Pages")
}

enum class GoalPeriod(val displayName: String) {
    YEAR("Year"),
    MONTH("Month"),
    WEEK("Week")
}

data class ReadingGoal(
    val id: String = UUID.randomUUID().toString(),
    val year: Int = 2026,
    val type: GoalType = GoalType.BOOKS,
    val period: GoalPeriod = GoalPeriod.YEAR,
    val targetCount: Int = 24,
    val currentProgress: Int = 0
) {
    val progressPercentage: Float
        get() = if (targetCount > 0) (currentProgress.toFloat() / targetCount).coerceIn(0f, 1f) else 0f
}
