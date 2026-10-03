package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.remote.BookSearchResult
import com.example.data.remote.OpenLibraryService
import com.example.data.repository.BookRepository
import com.example.model.Book
import com.example.model.BookFormat
import com.example.model.BookStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class AddBookUiState(
    val selectedTab: Int = 0, // 0 = Online Search, 1 = Manual Entry
    val manualEntrySubTab: Int = 0, // 0 = Form, 1 = JSON Import
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<BookSearchResult> = emptyList(),
    val searchError: String? = null,
    val isEditing: Boolean = false,

    // Form inputs
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val coverImageUrl: String = "",
    val totalChapters: Int = 10,
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val genresInput: String = "",
    val seriesName: String = "",
    val seriesPosition: String = "",
    val status: BookStatus = BookStatus.READING,
    val format: BookFormat = BookFormat.PHYSICAL,
    val bulkChapterTitlesInput: String = "",
    val isSaved: Boolean = false,
    val isSaving: Boolean = false,
    val formError: String? = null,
    val titleError: String? = null,
    val authorError: String? = null,
    val chaptersError: String? = null,
    val successMessage: String? = null,

    // JSON Import
    val jsonImportInput: String = "",
    val jsonImportError: String? = null,
    val jsonPreviewBook: Book? = null,
    val jsonPreviewChapters: List<com.example.domain.ParsedChapter> = emptyList()
)

class AddBookViewModel(
    private val bookRepository: BookRepository,
    private val openLibraryService: OpenLibraryService,
    rawEditBookId: String? = null
) : ViewModel() {

    private val editBookId: String? = rawEditBookId?.trim()?.takeIf {
        it.isNotEmpty() &&
        it != "{editBookId}" &&
        it != "null" &&
        it != "undefined" &&
        it != "new" &&
        !it.startsWith("{")
    }

    private val _uiState = MutableStateFlow(AddBookUiState())
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        if (!editBookId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isEditing = true,
                selectedTab = 1
            )
            viewModelScope.launch {
                val book = bookRepository.getBook(editBookId).firstOrNull()
                val chapters = bookRepository.getChapters(editBookId).firstOrNull() ?: emptyList()
                if (book != null) {
                    _uiState.value = _uiState.value.copy(
                        isEditing = true,
                        selectedTab = 1,
                        title = book.title,
                        author = book.author,
                        isbn = book.isbn,
                        coverImageUrl = book.coverImageUrl ?: "",
                        totalChapters = book.totalChapters,
                        totalPages = book.totalPages,
                        currentPage = book.currentPage,
                        genresInput = book.genres.joinToString(", "),
                        seriesName = book.seriesName ?: "",
                        seriesPosition = book.seriesPosition?.toString() ?: "",
                        status = book.status,
                        format = book.format,
                        bulkChapterTitlesInput = com.example.domain.ChapterParser.formatChapters(chapters)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isEditing = false,
                        formError = "Book with ID '$editBookId' was not found."
                    )
                }
            }
        }
    }

    fun setTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun setManualEntrySubTab(tab: Int) {
        _uiState.value = _uiState.value.copy(manualEntrySubTab = tab)
    }

    fun onJsonInputChanged(input: String) {
        _uiState.value = _uiState.value.copy(jsonImportInput = input, jsonImportError = null, jsonPreviewBook = null)
    }

    fun validateAndPreviewJson() {
        val jsonStr = _uiState.value.jsonImportInput.trim()
        if (jsonStr.isBlank()) {
            _uiState.value = _uiState.value.copy(jsonImportError = "JSON input is empty.")
            return
        }
        try {
            val root = JSONObject(jsonStr)
            val title = root.optString("title").trim()
            val author = root.optString("author").trim()
            if (title.isBlank() || author.isBlank()) {
                _uiState.value = _uiState.value.copy(jsonImportError = "JSON must contain non-empty 'title' and 'author' fields.")
                return
            }

            val genresList = mutableListOf<String>()
            val genresArr = root.optJSONArray("genres")
            if (genresArr != null) {
                for (j in 0 until genresArr.length()) {
                    genresList.add(genresArr.getString(j))
                }
            }

            val chaptersList = mutableListOf<com.example.domain.ParsedChapter>()
            val chArr = root.optJSONArray("chapters")
            if (chArr != null) {
                for (j in 0 until chArr.length()) {
                    val item = chArr.get(j)
                    if (item is JSONObject) {
                        val chTitle = item.optString("title").ifBlank { "Chapter ${j + 1}" }.trim()
                        val chPages = item.optInt("pageCount", item.optInt("pages", 0))
                        chaptersList.add(com.example.domain.ParsedChapter(title = chTitle, pageCount = chPages))
                    } else if (item is String) {
                        val parsed = com.example.domain.ChapterParser.parseLine(item, defaultChapterNumber = j + 1)
                        if (parsed != null) {
                            chaptersList.add(parsed)
                        }
                    }
                }
            }

            val chapterPagesSum = chaptersList.sumOf { it.pageCount }
            val totalCh = if (chaptersList.isNotEmpty()) chaptersList.size else root.optInt("totalChapters", 10)
            val specifiedPages = root.optInt("totalPages", 0)
            val totalPages = if (specifiedPages > 0) specifiedPages else chapterPagesSum

            val book = Book(
                id = UUID.randomUUID().toString(),
                title = title,
                author = author,
                isbn = root.optString("isbn", ""),
                coverImageUrl = root.optString("coverImageUrl").takeIf { it.isNotBlank() },
                totalChapters = totalCh,
                totalPages = totalPages,
                genres = genresList,
                status = BookStatus.fromString(root.optString("status", "READING")),
                format = BookFormat.fromString(root.optString("format", "PHYSICAL")),
                rating = root.optDouble("rating", 0.0).toFloat(),
                dateAdded = System.currentTimeMillis(),
                isFavorite = root.optBoolean("isFavorite", false),
                seriesName = root.optString("seriesName").takeIf { it.isNotBlank() },
                seriesPosition = root.optDouble("seriesPosition", 0.0).toFloat().takeIf { it > 0 }
            )

            _uiState.value = _uiState.value.copy(
                jsonPreviewBook = book,
                jsonPreviewChapters = chaptersList,
                jsonImportError = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(jsonImportError = "Invalid JSON format: ${e.localizedMessage}")
        }
    }

    fun importFromJson() {
        val preview = _uiState.value.jsonPreviewBook ?: return
        val chapters = _uiState.value.jsonPreviewChapters
        viewModelScope.launch {
            bookRepository.insertBookWithParsedChapters(preview, chapters)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false, searchError = null)
            return
        }

        searchJob = viewModelScope.launch {
            delay(500L) // Debounce
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
            try {
                val response = openLibraryService.searchBooks(query)
                val searchResults = response.docs.map { doc ->
                    BookSearchResult(
                        title = doc.title,
                        author = doc.author,
                        isbn = doc.firstIsbn,
                        coverImageUrl = doc.coverUrl,
                        estimatedPages = doc.numberOfPagesMedian ?: 250,
                        subjects = doc.subject ?: emptyList()
                    )
                }
                _uiState.value = _uiState.value.copy(
                    searchResults = searchResults,
                    isSearching = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    searchError = "Search failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun selectSearchResult(result: BookSearchResult) {
        _uiState.value = _uiState.value.copy(
            title = result.title,
            author = result.author,
            isbn = result.isbn,
            coverImageUrl = result.coverImageUrl ?: "",
            totalChapters = 12,
            totalPages = if (result.estimatedPages > 0) result.estimatedPages else 300,
            selectedTab = 1 // Switch to Manual Entry to review
        )
    }

    fun searchByIsbn(isbn: String) {
        val clean = isbn.filter { it.isDigit() || it.equals('X', ignoreCase = true) }
        if (clean.length < 10) {
            _uiState.value = _uiState.value.copy(searchError = "Please enter a valid ISBN (at least 10 characters).")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
            try {
                val response = openLibraryService.searchByIsbn(clean)
                val results = response.docs.map { doc ->
                    BookSearchResult(
                        title = doc.title,
                        author = doc.author,
                        isbn = doc.firstIsbn,
                        coverImageUrl = doc.coverUrl,
                        estimatedPages = doc.numberOfPagesMedian ?: 250,
                        subjects = doc.subject ?: emptyList()
                    )
                }
                if (results.isNotEmpty()) {
                    val res = results.first()
                    _uiState.value = _uiState.value.copy(
                        title = res.title,
                        author = res.author,
                        isbn = res.isbn.ifBlank { clean },
                        coverImageUrl = res.coverImageUrl ?: "",
                        totalChapters = 12,
                        totalPages = if (res.estimatedPages > 0) res.estimatedPages else 300,
                        isSearching = false,
                        selectedTab = 1
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        searchResults = emptyList(),
                        isSearching = false,
                        searchError = "No books found for ISBN $clean. You can enter details directly in Manual Entry."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    searchError = "Could not fetch ISBN details: ${e.localizedMessage}"
                )
            }
        }
    }

    fun updateField(
        title: String = _uiState.value.title,
        author: String = _uiState.value.author,
        isbn: String = _uiState.value.isbn,
        coverImageUrl: String = _uiState.value.coverImageUrl,
        totalChapters: Int = _uiState.value.totalChapters,
        totalPages: Int = _uiState.value.totalPages,
        currentPage: Int = _uiState.value.currentPage,
        genresInput: String = _uiState.value.genresInput,
        seriesName: String = _uiState.value.seriesName,
        seriesPosition: String = _uiState.value.seriesPosition,
        status: BookStatus = _uiState.value.status,
        format: BookFormat = _uiState.value.format,
        bulkChapterTitlesInput: String = _uiState.value.bulkChapterTitlesInput
    ) {
        _uiState.value = _uiState.value.copy(
            title = title,
            author = author,
            isbn = isbn,
            coverImageUrl = coverImageUrl,
            totalChapters = totalChapters,
            totalPages = totalPages,
            currentPage = currentPage,
            genresInput = genresInput,
            seriesName = seriesName,
            seriesPosition = seriesPosition,
            status = status,
            format = format,
            bulkChapterTitlesInput = bulkChapterTitlesInput
        )
    }

    fun onIsbnChange(v: String) { _uiState.value = _uiState.value.copy(isbn = v, formError = null) }
    fun onTitleChange(v: String) { _uiState.value = _uiState.value.copy(title = v, titleError = null, formError = null) }
    fun onAuthorChange(v: String) { _uiState.value = _uiState.value.copy(author = v, authorError = null, formError = null) }
    fun onCoverImageUrlChange(v: String) { _uiState.value = _uiState.value.copy(coverImageUrl = v, formError = null) }
    fun onSeriesNameChange(v: String) { _uiState.value = _uiState.value.copy(seriesName = v, formError = null) }
    fun onSeriesPositionChange(v: String) { _uiState.value = _uiState.value.copy(seriesPosition = v, formError = null) }
    fun onChaptersChange(v: Int) { _uiState.value = _uiState.value.copy(totalChapters = v, chaptersError = null, formError = null) }
    fun onPagesChange(v: Int) { _uiState.value = _uiState.value.copy(totalPages = v, formError = null) }
    fun onCurrentPageChange(v: Int) { _uiState.value = _uiState.value.copy(currentPage = v, formError = null) }
    fun onGenresChange(v: String) { _uiState.value = _uiState.value.copy(genresInput = v, formError = null) }
    fun onFormatChange(v: BookFormat) { _uiState.value = _uiState.value.copy(format = v, formError = null) }
    fun onStatusChange(v: BookStatus) { _uiState.value = _uiState.value.copy(status = v, formError = null) }
    fun onBulkChaptersChange(v: String) { _uiState.value = _uiState.value.copy(bulkChapterTitlesInput = v, chaptersError = null, formError = null) }

    fun clearFormError() {
        _uiState.value = _uiState.value.copy(formError = null, titleError = null, authorError = null, chaptersError = null)
    }

    fun saveBook() {
        if (_uiState.value.isSaving) return

        val s = _uiState.value
        val trimmedTitle = s.title.trim()
        val trimmedAuthor = s.author.trim()

        var hasError = false
        var titleErr: String? = null
        var authorErr: String? = null
        var chaptersErr: String? = null
        var generalErr: String? = null

        if (trimmedTitle.isBlank()) {
            titleErr = "Book title is required"
            hasError = true
        }

        if (trimmedAuthor.isBlank()) {
            authorErr = "Author is required"
            hasError = true
        }

        val parsedChapters = if (s.bulkChapterTitlesInput.isNotBlank()) {
            com.example.domain.ChapterParser.parseBulkText(s.bulkChapterTitlesInput)
        } else {
            emptyList()
        }

        val invalidChapter = parsedChapters.firstOrNull { it.hasError }
        if (invalidChapter != null) {
            chaptersErr = invalidChapter.errorMessage ?: "Invalid chapter format. Use 'Chapter Title|Pages'."
            generalErr = chaptersErr
            hasError = true
        }

        val totalCh = if (parsedChapters.isNotEmpty()) {
            parsedChapters.size
        } else if (s.totalChapters > 0) {
            s.totalChapters
        } else {
            0
        }

        if (totalCh <= 0) {
            chaptersErr = "At least 1 chapter is required"
            hasError = true
        }

        if (hasError) {
            val firstMsg = generalErr ?: titleErr ?: authorErr ?: chaptersErr ?: "Please fill in all required fields."
            _uiState.value = _uiState.value.copy(
                formError = firstMsg,
                titleError = titleErr,
                authorError = authorErr,
                chaptersError = chaptersErr
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isSaving = true,
            formError = null,
            titleError = null,
            authorError = null,
            chaptersError = null
        )

        viewModelScope.launch {
            try {
                val genresList = s.genresInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val chapterPagesSum = parsedChapters.sumOf { it.pageCount }
                val totalPages = if (s.totalPages > 0) s.totalPages else if (chapterPagesSum > 0) chapterPagesSum else 0
                val currentPage = s.currentPage.coerceIn(0, if (totalPages > 0) totalPages else 999999)

                if (!editBookId.isNullOrBlank()) {
                    val existingBook = bookRepository.getBook(editBookId).firstOrNull()
                    if (existingBook != null) {
                        val updatedBook = existingBook.copy(
                            title = trimmedTitle,
                            author = trimmedAuthor,
                            isbn = s.isbn.trim(),
                            coverImageUrl = s.coverImageUrl.trim().takeIf { it.isNotBlank() },
                            totalChapters = totalCh,
                            totalPages = totalPages,
                            currentPage = currentPage,
                            genres = genresList,
                            seriesName = s.seriesName.trim().takeIf { it.isNotBlank() },
                            seriesPosition = s.seriesPosition.trim().toFloatOrNull(),
                            status = s.status,
                            format = s.format
                        )
                        bookRepository.updateBook(updatedBook)
                        if (parsedChapters.isNotEmpty()) {
                            bookRepository.bulkUpdateChapters(editBookId, parsedChapters)
                        }
                    } else {
                        throw IllegalStateException("Book with ID '$editBookId' was not found for editing.")
                    }
                } else {
                    val now = System.currentTimeMillis()
                    val newBookId = UUID.randomUUID().toString()
                    val book = Book(
                        id = newBookId,
                        title = trimmedTitle,
                        author = trimmedAuthor,
                        isbn = s.isbn.trim(),
                        coverImageUrl = s.coverImageUrl.trim().takeIf { it.isNotBlank() },
                        totalChapters = totalCh,
                        totalPages = totalPages,
                        currentPage = currentPage,
                        genres = genresList,
                        seriesName = s.seriesName.trim().takeIf { it.isNotBlank() },
                        seriesPosition = s.seriesPosition.trim().toFloatOrNull(),
                        status = s.status,
                        format = s.format,
                        rating = 0f,
                        dateAdded = now,
                        dateStarted = if (s.status == BookStatus.READING) now else null,
                        dateFinished = if (s.status == BookStatus.FINISHED) now else null
                    )
                    bookRepository.insertBookWithParsedChapters(book, parsedChapters)
                }
                _uiState.value = _uiState.value.copy(
                    isSaved = true,
                    isSaving = false,
                    formError = null,
                    successMessage = if (!editBookId.isNullOrBlank()) "Book updated successfully" else "Book added successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    formError = "Failed to save book: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun quickAddBook(result: BookSearchResult) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val totalCh = 12
                val totalPages = if (result.estimatedPages > 0) result.estimatedPages else 300
                val book = Book(
                    id = UUID.randomUUID().toString(),
                    title = result.title.trim().ifBlank { "Untitled" },
                    author = result.author.trim().ifBlank { "Unknown Author" },
                    isbn = result.isbn.trim(),
                    coverImageUrl = result.coverImageUrl?.takeIf { it.isNotBlank() },
                    totalChapters = totalCh,
                    totalPages = totalPages,
                    currentPage = 0,
                    genres = result.subjects.take(3),
                    status = BookStatus.READING,
                    format = BookFormat.PHYSICAL,
                    rating = 0f,
                    dateAdded = now,
                    dateStarted = now
                )
                bookRepository.insertBookWithParsedChapters(book, emptyList())
                _uiState.value = _uiState.value.copy(isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    formError = "Failed to quick add: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    class Factory(
        private val bookRepository: BookRepository,
        private val openLibraryService: OpenLibraryService,
        private val editBookId: String? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddBookViewModel(bookRepository, openLibraryService, editBookId) as T
        }
    }
}
