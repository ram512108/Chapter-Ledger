package com.example.data.repository

import com.example.data.local.dao.GoalDao
import com.example.data.local.entity.ReadingGoalEntity
import com.example.model.ReadingGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepository(
    private val goalDao: GoalDao
) {
    fun getGoalsForYear(year: Int): Flow<List<ReadingGoal>> =
        goalDao.getGoalsForYear(year).map { list -> list.map { it.toDomain() } }

    suspend fun saveGoal(goal: ReadingGoal) {
        goalDao.insertGoal(ReadingGoalEntity.fromDomain(goal))
    }

    suspend fun deleteGoal(id: String) {
        goalDao.deleteGoalById(id)
    }
}
