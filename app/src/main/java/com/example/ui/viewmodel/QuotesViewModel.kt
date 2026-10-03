package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BookRepository
import com.example.model.Book
import com.example.model.Chapter
import com.example.ui.components.ChapterOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuoteItem(
    val id: String,
    val chapterId: String,
    val bookId: String,
    val bookTitle: String,
    val author: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val pageNumber: Int,
    val quoteText: String,
    val notes: String,
    val isFavorite: Boolean,
    val quoteIndex: Int
)

data class QuotesUiState(
    val allQuotes: List<QuoteItem> = emptyList(),
    val filteredQuotes: List<QuoteItem> = emptyList(),
    val searchQuery: String = "",
    val books: List<Book> = emptyList(),
    val selectedBookId: String? = null,
    val bookChaptersMap: Map<String, List<ChapterOption>> = emptyMap()
)

class QuotesViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedBookId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<QuotesUiState> = combine(
        bookRepository.allNotesAndQuotes,
        bookRepository.allBooks,
        _searchQuery,
        _selectedBookId
    ) { chapters, books, query, bookId ->
        val bookMap = books.associateBy { it.id }
        val items = mutableListOf<QuoteItem>()
        val chapterMap = mutableMapOf<String, MutableList<ChapterOption>>()

        chapters.forEach { ch ->
            val book = bookMap[ch.bookId]
            val bTitle = book?.title ?: "Unknown Book"
            val bAuthor = book?.author ?: "Unknown Author"
            val bFav = book?.isFavorite ?: false

            val chList = chapterMap.getOrPut(ch.bookId) { mutableListOf() }
            if (chList.none { it.id == ch.id }) {
                chList.add(ChapterOption(id = ch.id, number = ch.number, title = ch.displayTitle))
            }

            ch.highlightedQuotes.forEachIndexed { index, quote ->
                if (quote.isNotBlank()) {
                    items.add(
                        QuoteItem(
                            id = "${ch.id}_${index}_${quote.hashCode()}",
                            chapterId = ch.id,
                            bookId = ch.bookId,
                            bookTitle = bTitle,
                            author = bAuthor,
                            chapterNumber = ch.number,
                            chapterTitle = ch.displayTitle,
                            pageNumber = ch.pageCount,
                            quoteText = quote,
                            notes = ch.notes,
                            isFavorite = bFav || ch.rating >= 4.0f,
                            quoteIndex = index
                        )
                    )
                }
            }
        }

        var filtered: List<QuoteItem> = items
        if (bookId != null) {
            filtered = filtered.filter { it.bookId == bookId }
        }
        if (query.isNotBlank()) {
            val q = query.lowercase().trim()
            filtered = filtered.filter {
                it.quoteText.lowercase().contains(q) ||
                        it.bookTitle.lowercase().contains(q) ||
                        it.author.lowercase().contains(q) ||
                        it.chapterTitle.lowercase().contains(q) ||
                        it.notes.lowercase().contains(q)
            }
        }

        QuotesUiState(
            allQuotes = items,
            filteredQuotes = filtered,
            searchQuery = query,
            books = books,
            selectedBookId = bookId,
            bookChaptersMap = chapterMap.mapValues { it.value.sortedBy { opt -> opt.number } }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuotesUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSelectBook(bookId: String?) {
        _selectedBookId.value = if (_selectedBookId.value == bookId) null else bookId
    }

    fun updateQuote(
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

    class Factory(private val bookRepository: BookRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuotesViewModel(bookRepository) as T
        }
    }
}
