package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.GoalPeriod
import com.example.model.GoalType
import com.example.model.ReadingGoal

@Entity(tableName = "reading_goals")
data class ReadingGoalEntity(
    @PrimaryKey
    val id: String,
    val year: Int,
    val type: String,
    val period: String,
    val targetCount: Int
) {
    fun toDomain(progress: Int = 0): ReadingGoal {
        return ReadingGoal(
            id = id,
            year = year,
            type = GoalType.entries.find { it.name.equals(type, ignoreCase = true) } ?: GoalType.BOOKS,
            period = GoalPeriod.entries.find { it.name.equals(period, ignoreCase = true) } ?: GoalPeriod.YEAR,
            targetCount = targetCount,
            currentProgress = progress
        )
    }

    companion object {
        fun fromDomain(goal: ReadingGoal): ReadingGoalEntity {
            return ReadingGoalEntity(
                id = goal.id,
                year = goal.year,
                type = goal.type.name,
                period = goal.period.name,
                targetCount = goal.targetCount
            )
        }
    }
}
