package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BookRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.ReadingSessionRepository
import com.example.model.ActiveReadingSession
import com.example.model.Book
import com.example.model.BookStatus
import com.example.model.Chapter
import com.example.model.ReadingSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BookDetailUiState(
    val book: Book? = null,
    val chapters: List<Chapter> = emptyList(),
    val readingOrderTimeline: List<Chapter> = emptyList(),
    val selectedChapterForDetails: Chapter? = null,
    val showBulkEditor: Boolean = false,
    val showReadingSessionSheet: Boolean = false,
    val activeSession: ActiveReadingSession? = null,
    val sessions: List<ReadingSession> = emptyList()
)

class BookDetailViewModel(
    private val bookId: String,
    private val bookRepository: BookRepository,
    private val sessionRepository: ReadingSessionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _selectedChapterId = MutableStateFlow<String?>(null)
    private val _showBulkEditor = MutableStateFlow(false)
    private val _showSessionSheet = MutableStateFlow(false)

    val uiState: StateFlow<BookDetailUiState> = combine(
        bookRepository.getBook(bookId),
        bookRepository.getChapters(bookId),
        bookRepository.getReadingOrderTimeline(bookId),
        sessionRepository.getSessionsForBook(bookId),
        preferencesRepository.activeReadingSession,
        _selectedChapterId,
        _showBulkEditor,
        _showSessionSheet
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val book = args[0] as Book?
        @Suppress("UNCHECKED_CAST")
        val chapters = args[1] as List<Chapter>
        @Suppress("UNCHECKED_CAST")
        val readingOrderTimeline = args[2] as List<Chapter>
        @Suppress("UNCHECKED_CAST")
        val sessions = args[3] as List<ReadingSession>
        val activeSession = args[4] as ActiveReadingSession?
        val selectedChapterId = args[5] as String?
        val showBulkEditor = args[6] as Boolean
        val showReadingSessionSheet = args[7] as Boolean

        val selectedChapter = selectedChapterId?.let { id ->
            chapters.find { it.id == id }
        }

        // Auto-show session sheet if an active session exists for this book and was not explicitly dismissed
        val isSheetVisible = showReadingSessionSheet || (activeSession != null && activeSession.bookId == bookId && showReadingSessionSheet)

        BookDetailUiState(
            book = book,
            chapters = chapters,
            readingOrderTimeline = readingOrderTimeline,
            sessions = sessions,
            selectedChapterForDetails = selectedChapter,
            showBulkEditor = showBulkEditor,
            showReadingSessionSheet = isSheetVisible,
            activeSession = activeSession
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookDetailUiState()
    )

    fun toggleChapter(chapterId: String) {
        viewModelScope.launch {
            bookRepository.toggleChapterRead(bookId, chapterId)
        }
    }

    fun pinChapter(chapterId: String) {
        viewModelScope.launch {
            bookRepository.pinChapterNext(bookId, chapterId)
        }
    }

    fun unpinChapter(chapterId: String) {
        viewModelScope.launch {
            bookRepository.unpinChapter(bookId, chapterId)
        }
    }

    fun selectChapterForDetails(chapter: Chapter?) {
        _selectedChapterId.value = chapter?.id
    }

    fun saveChapterDetails(
        chapterId: String,
        customTitle: String,
        notes: String,
        quotes: List<String>,
        rating: Float,
        pageCount: Int = 0
    ) {
        viewModelScope.launch {
            bookRepository.updateChapterDetails(
                chapterId = chapterId,
                customTitle = customTitle,
                notes = notes,
                quotes = quotes,
                rating = rating,
                pageCount = pageCount
            )
            _selectedChapterId.value = null
        }
    }

    fun setBulkEditorVisible(visible: Boolean) {
        _showBulkEditor.value = visible
    }

    fun saveBulkChapters(chapters: List<com.example.domain.ParsedChapter>) {
        viewModelScope.launch {
            bookRepository.bulkUpdateChapters(bookId, chapters)
            _showBulkEditor.value = false
        }
    }

    fun updateSavedQuote(
        chapterId: String,
        quoteIndex: Int,
        newQuoteText: String,
        newNotes: String,
        newPageNumber: Int,
        targetChapterId: String,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            bookRepository.updateSavedQuote(
                chapterId = chapterId,
                quoteIndex = quoteIndex,
                newQuoteText = newQuoteText,
                newNotes = newNotes,
                newPageCount = newPageNumber,
                targetChapterId = targetChapterId,
                isFavorite = isFavorite
            )
        }
    }

    fun startOrResumeReadingSession() {
        viewModelScope.launch {
            val book = uiState.value.book
            val title = book?.title ?: "Book"
            preferencesRepository.startOrGetActiveSession(bookId, title)
            _showSessionSheet.value = true
        }
    }

    fun setReadingSessionSheetVisible(visible: Boolean) {
        if (visible) {
            startOrResumeReadingSession()
        } else {
            // Dismiss popup only - active session continues in background
            _showSessionSheet.value = false
        }
    }

    fun toggleSessionTimer() {
        viewModelScope.launch {
            preferencesRepository.toggleActiveSessionTimer()
        }
    }

    fun updateSessionDraft(chapters: String, pages: String, notes: String) {
        viewModelScope.launch {
            preferencesRepository.updateActiveSessionDraft(chapters, pages, notes)
        }
    }

    fun resetActiveSession() {
        viewModelScope.launch {
            preferencesRepository.clearActiveReadingSession()
            _showSessionSheet.value = false
        }
    }

    fun finishReadingSession(session: ReadingSession) {
        viewModelScope.launch {
            sessionRepository.insertSession(session)
            if (session.pagesCovered > 0) {
                val currentBook = uiState.value.book
                if (currentBook != null) {
                    val newPage = (currentBook.currentPage + session.pagesCovered).coerceIn(
                        0,
                        if (currentBook.totalPages > 0) currentBook.totalPages else 99999
                    )
                    bookRepository.updateCurrentPage(bookId, newPage)
                    if (currentBook.totalPages > 0 && newPage >= currentBook.totalPages && currentBook.status != BookStatus.FINISHED) {
                        bookRepository.updateBookStatus(bookId, BookStatus.FINISHED)
                    }
                }
            }
            preferencesRepository.clearActiveReadingSession()
            _showSessionSheet.value = false
        }
    }

    fun updateCurrentPage(page: Int) {
        viewModelScope.launch {
            val valid = page.coerceAtLeast(0)
            bookRepository.updateCurrentPage(bookId, valid)
            val currentBook = uiState.value.book
            if (currentBook != null && currentBook.totalPages > 0 && valid >= currentBook.totalPages && currentBook.status != BookStatus.FINISHED) {
                bookRepository.updateBookStatus(bookId, BookStatus.FINISHED)
            }
        }
    }

    fun incrementCurrentPage(delta: Int) {
        viewModelScope.launch {
            val currentBook = uiState.value.book ?: return@launch
            val newPage = (currentBook.currentPage + delta).coerceIn(
                0,
                if (currentBook.totalPages > 0) currentBook.totalPages else 99999
            )
            bookRepository.updateCurrentPage(bookId, newPage)
            if (currentBook.totalPages > 0 && newPage >= currentBook.totalPages && currentBook.status != BookStatus.FINISHED) {
                bookRepository.updateBookStatus(bookId, BookStatus.FINISHED)
            }
        }
    }

    fun updateTotalPages(pages: Int) {
        viewModelScope.launch {
            bookRepository.updateTotalPages(bookId, pages.coerceAtLeast(0))
        }
    }

    fun deleteBook(onDeleted: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.deleteSessionsForBook(bookId)
            bookRepository.deleteBook(bookId)
            onDeleted()
        }
    }

    fun updateBookStatus(status: BookStatus) {
        viewModelScope.launch {
            bookRepository.updateBookStatus(bookId, status)
        }
    }

    fun updateBookRating(rating: Float) {
        viewModelScope.launch {
            bookRepository.updateBookRating(bookId, rating)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val current = uiState.value.book?.isFavorite ?: false
            bookRepository.toggleFavorite(bookId, !current)
        }
    }

    class Factory(
        private val bookId: String,
        private val bookRepository: BookRepository,
        private val sessionRepository: ReadingSessionRepository,
        private val preferencesRepository: PreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BookDetailViewModel(bookId, bookRepository, sessionRepository, preferencesRepository) as T
        }
    }
}
