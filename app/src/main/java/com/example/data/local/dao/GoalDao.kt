package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ReadingGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM reading_goals WHERE year = :year")
    fun getGoalsForYear(year: Int): Flow<List<ReadingGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: ReadingGoalEntity)

    @Update
    suspend fun updateGoal(goal: ReadingGoalEntity)

    @Query("DELETE FROM reading_goals WHERE id = :id")
    suspend fun deleteGoalById(id: String)
}
