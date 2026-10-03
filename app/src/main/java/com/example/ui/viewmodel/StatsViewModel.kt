package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChapterLedgerDatabase
import com.example.data.repository.BookRepository
import com.example.data.repository.GoalRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.ReadingSessionRepository
import com.example.domain.StatsCalculator
import com.example.model.GoalPeriod
import com.example.model.GoalType
import com.example.model.ReadingGoal
import com.example.model.ReadingSession
import com.example.model.ReadingStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class StatsUiState(
    val stats: ReadingStats = ReadingStats(),
    val annualGoalTarget: Int = 24,
    val currentYear: Int = 2026,
    val recentSessions: List<ReadingSession> = emptyList(),
    val isLoading: Boolean = false
)

class StatsViewModel(
    private val bookRepository: BookRepository,
    private val sessionRepository: ReadingSessionRepository,
    private val goalRepository: GoalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val database: ChapterLedgerDatabase
) : ViewModel() {

    private val _stats = MutableStateFlow(ReadingStats())
    private val _isLoading = MutableStateFlow(true)
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    init {
        refreshStats()
    }

    val uiState: StateFlow<StatsUiState> = combine(
        _stats,
        preferencesRepository.annualBookGoal,
        sessionRepository.allSessions,
        _isLoading
    ) { stats, annualGoal, sessions, loading ->
        StatsUiState(
            stats = stats,
            annualGoalTarget = annualGoal,
            currentYear = currentYear,
            recentSessions = sessions.take(10),
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    fun refreshStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val books = bookRepository.allBooks.first()
                val calculated = StatsCalculator.calculateStats(
                    books = books,
                    chapterDao = database.chapterDao(),
                    sessionDao = database.readingSessionDao()
                )
                _stats.value = calculated
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAnnualGoal(target: Int) {
        viewModelScope.launch {
            preferencesRepository.setAnnualBookGoal(target)
            goalRepository.saveGoal(
                ReadingGoal(
                    id = "annual_goal_$currentYear",
                    year = currentYear,
                    type = GoalType.BOOKS,
                    period = GoalPeriod.YEAR,
                    targetCount = target,
                    currentProgress = _stats.value.booksThisYear
                )
            )
        }
    }

    class Factory(
        private val bookRepository: BookRepository,
        private val sessionRepository: ReadingSessionRepository,
        private val goalRepository: GoalRepository,
        private val preferencesRepository: PreferencesRepository,
        private val database: ChapterLedgerDatabase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(
                bookRepository,
                sessionRepository,
                goalRepository,
                preferencesRepository,
                database
            ) as T
        }
    }
}
