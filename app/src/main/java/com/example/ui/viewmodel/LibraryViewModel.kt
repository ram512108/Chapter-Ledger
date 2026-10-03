package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BookRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.ReadingSessionRepository
import com.example.model.ActiveReadingSession
import com.example.model.Book
import com.example.model.BookFormat
import com.example.model.BookStatus
import com.example.model.ReadingSession
import com.example.model.Shelf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val displayName: String) {
    RECENTLY_ADDED("Recently Added"),
    PROGRESS("Progress %"),
    TITLE("Title (A-Z)"),
    AUTHOR("Author"),
    RATING("Highest Rated")
}

data class LibraryUiState(
    val books: List<Book> = emptyList(),
    val filteredBooks: List<Book> = emptyList(),
    val shelves: List<Shelf> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: BookStatus? = null,
    val selectedFormat: BookFormat? = null,
    val selectedShelfId: String? = null,
    val favoritesOnly: Boolean = false,
    val sortOption: SortOption = SortOption.RECENTLY_ADDED,
    val isGridMode: Boolean = true,
    val totalBooksCount: Int = 0,
    val currentlyReadingCount: Int = 0,
    val completedCount: Int = 0,
    val activeSession: ActiveReadingSession? = null
)

class LibraryViewModel(
    private val bookRepository: BookRepository,
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: ReadingSessionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatus = MutableStateFlow<BookStatus?>(null)
    private val _selectedFormat = MutableStateFlow<BookFormat?>(null)
    private val _selectedShelfId = MutableStateFlow<String?>(null)
    private val _favoritesOnly = MutableStateFlow(false)
    private val _sortOption = MutableStateFlow(SortOption.RECENTLY_ADDED)

    val uiState: StateFlow<LibraryUiState> = combine(
        bookRepository.allBooks,
        bookRepository.allShelves,
        preferencesRepository.isGridMode,
        preferencesRepository.activeReadingSession,
        _searchQuery,
        _selectedStatus,
        _selectedFormat,
        _selectedShelfId,
        _favoritesOnly,
        _sortOption
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val books = args[0] as List<Book>
        @Suppress("UNCHECKED_CAST")
        val shelves = args[1] as List<Shelf>
        val isGrid = args[2] as Boolean
        val activeSession = args[3] as ActiveReadingSession?
        val query = args[4] as String
        val status = args[5] as BookStatus?
        val format = args[6] as BookFormat?
        val shelfId = args[7] as String?
        val favOnly = args[8] as Boolean
        val sort = args[9] as SortOption

        var filtered = books

        // Filter by search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                        it.author.lowercase().contains(q) ||
                        it.genres.any { g -> g.lowercase().contains(q) } ||
                        it.seriesName?.lowercase()?.contains(q) == true
            }
        }

        // Filter by status
        if (status != null) {
            filtered = filtered.filter { it.status == status }
        }

        // Filter by format
        if (format != null) {
            filtered = filtered.filter { it.format == format }
        }

        // Filter by shelf
        if (shelfId != null) {
            filtered = filtered.filter { it.customShelves.contains(shelfId) }
        }

        // Filter favorites
        if (favOnly) {
            filtered = filtered.filter { it.isFavorite }
        }

        // Sort
        filtered = when (sort) {
            SortOption.RECENTLY_ADDED -> filtered.sortedByDescending { it.dateAdded }
            SortOption.PROGRESS -> filtered.sortedByDescending { it.progressPercentage }
            SortOption.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortOption.AUTHOR -> filtered.sortedBy { it.author.lowercase() }
            SortOption.RATING -> filtered.sortedByDescending { it.rating }
        }

        LibraryUiState(
            books = books,
            filteredBooks = filtered,
            shelves = shelves,
            searchQuery = query,
            selectedStatus = status,
            selectedFormat = format,
            selectedShelfId = shelfId,
            favoritesOnly = favOnly,
            sortOption = sort,
            isGridMode = isGrid,
            totalBooksCount = books.size,
            currentlyReadingCount = books.count { it.status == BookStatus.READING },
            completedCount = books.count { it.status == BookStatus.FINISHED },
            activeSession = activeSession
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusSelected(status: BookStatus?) {
        _selectedStatus.value = if (_selectedStatus.value == status) null else status
    }

    fun onFormatSelected(format: BookFormat?) {
        _selectedFormat.value = if (_selectedFormat.value == format) null else format
    }

    fun onShelfSelected(shelfId: String?) {
        _selectedShelfId.value = if (_selectedShelfId.value == shelfId) null else shelfId
    }

    fun onToggleFavoritesOnly() {
        _favoritesOnly.value = !_favoritesOnly.value
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedStatus.value = null
        _selectedFormat.value = null
        _selectedShelfId.value = null
        _favoritesOnly.value = false
    }

    fun onSortSelected(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleGridMode() {
        viewModelScope.launch {
            val current = uiState.value.isGridMode
            preferencesRepository.setGridMode(!current)
        }
    }

    fun toggleFavorite(bookId: String, isFav: Boolean) {
        viewModelScope.launch {
            bookRepository.toggleFavorite(bookId, isFav)
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSessionsForBook(bookId)
            bookRepository.deleteBook(bookId)
        }
    }

    fun createShelf(name: String, description: String = "", color: String = "#F59E0B") {
        viewModelScope.launch {
            bookRepository.createShelf(name, description, color)
        }
    }

    fun toggleActiveSessionTimer() {
        viewModelScope.launch {
            preferencesRepository.toggleActiveSessionTimer()
        }
    }

    fun updateActiveSessionDraft(chapters: String, pages: String, notes: String) {
        viewModelScope.launch {
            preferencesRepository.updateActiveSessionDraft(chapters, pages, notes)
        }
    }

    fun resetActiveSession() {
        viewModelScope.launch {
            preferencesRepository.clearActiveReadingSession()
        }
    }

    fun finishActiveSession(session: ReadingSession) {
        viewModelScope.launch {
            sessionRepository.insertSession(session)
            if (session.pagesCovered > 0) {
                val book = uiState.value.books.find { it.id == session.bookId }
                if (book != null) {
                    val newPage = (book.currentPage + session.pagesCovered).coerceIn(
                        0,
                        if (book.totalPages > 0) book.totalPages else 99999
                    )
                    bookRepository.updateCurrentPage(book.id, newPage)
                    if (book.totalPages > 0 && newPage >= book.totalPages && book.status != BookStatus.FINISHED) {
                        bookRepository.updateBookStatus(book.id, BookStatus.FINISHED)
                    }
                }
            }
            preferencesRepository.clearActiveReadingSession()
        }
    }

    class Factory(
        private val bookRepository: BookRepository,
        private val preferencesRepository: PreferencesRepository,
        private val sessionRepository: ReadingSessionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(bookRepository, preferencesRepository, sessionRepository) as T
        }
    }
}
