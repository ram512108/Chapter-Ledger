package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChapterLedgerDatabase
import com.example.data.repository.BookRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.ReadingSessionRepository
import com.example.data.repository.SampleData
import com.example.domain.ExportImportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val annualGoal: Int = 24,
    val message: String? = null,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportedJson: String? = null
)

class SettingsViewModel(
    private val bookRepository: BookRepository,
    private val sessionRepository: ReadingSessionRepository,
    private val preferencesRepository: PreferencesRepository,
    private val database: ChapterLedgerDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val goal = preferencesRepository.annualBookGoal.first()
            _uiState.value = _uiState.value.copy(annualGoal = goal)
        }
    }

    fun exportToJson() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val books = bookRepository.allBooks.first()
                val json = ExportImportManager.exportToJson(books)
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportedJson = json,
                    message = "Exported ${books.size} books successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    message = "Export failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun importFromJson(jsonString: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val count = ExportImportManager.importFromJson(jsonString, bookRepository)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = "Imported $count books from backup successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = "JSON Import error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun importGoodreadsCsv(csvContent: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val count = ExportImportManager.importGoodreadsCsv(csvContent, bookRepository)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = "Imported $count books from Goodreads export!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = "Goodreads CSV error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun restoreSampleData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                SampleData.seedDatabaseIfEmpty(
                    bookDao = database.bookDao(),
                    chapterDao = database.chapterDao(),
                    sessionDao = database.readingSessionDao(),
                    shelfDao = database.shelfDao(),
                    preferencesRepository = preferencesRepository
                )
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = "Sample library data refreshed successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = "Error loading sample data: ${e.localizedMessage}"
                )
            }
        }
    }

    fun updateAnnualGoal(goal: Int) {
        viewModelScope.launch {
            preferencesRepository.setAnnualBookGoal(goal)
            _uiState.value = _uiState.value.copy(annualGoal = goal)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, exportedJson = null)
    }

    class Factory(
        private val bookRepository: BookRepository,
        private val sessionRepository: ReadingSessionRepository,
        private val preferencesRepository: PreferencesRepository,
        private val database: ChapterLedgerDatabase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                bookRepository,
                sessionRepository,
                preferencesRepository,
                database
            ) as T
        }
    }
}
