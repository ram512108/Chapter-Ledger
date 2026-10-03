package com.example.data.repository

import com.example.data.local.dao.BookDao
import com.example.data.local.dao.ChapterDao
import com.example.data.local.dao.ShelfDao
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.ShelfEntity
import com.example.model.Book
import com.example.model.BookFormat
import com.example.model.BookStatus
import com.example.model.Chapter
import com.example.model.Shelf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class BookRepository(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val shelfDao: ShelfDao
) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooksWithProgress().map { list ->
        list.map { it.toDomain() }
    }

    val allShelves: Flow<List<Shelf>> = shelfDao.getAllShelves().map { list ->
        list.map { it.toDomain() }
    }

    val allNotesAndQuotes: Flow<List<Chapter>> = chapterDao.getAllNotesAndQuotes().map { list ->
        list.map { it.toDomain() }
    }

    fun getBook(bookId: String): Flow<Book?> = bookDao.getBookById(bookId).map { it?.toDomain() }

    fun getChapters(bookId: String): Flow<List<Chapter>> = chapterDao.getChaptersForBook(bookId).map { list ->
        list.map { it.toDomain() }
    }

    fun getReadingOrderTimeline(bookId: String): Flow<List<Chapter>> = chapterDao.getReadingOrderTimeline(bookId).map { list ->
        list.map { it.toDomain() }
    }

    suspend fun insertBookWithChapters(book: Book, chapterTitles: List<String> = emptyList()) {
        val parsedEntries = chapterTitles.mapNotNull { com.example.domain.ChapterParser.parseLine(it) }
        insertBookWithParsedChapters(book, parsedEntries)
    }

    suspend fun insertBookWithParsedChapters(book: Book, chapterEntries: List<com.example.domain.ParsedChapter> = emptyList()) {
        val chapterPagesSum = chapterEntries.sumOf { it.pageCount }
        val finalTotalPages = if (book.totalPages > 0) book.totalPages else chapterPagesSum
        val finalTotalChapters = if (chapterEntries.isNotEmpty()) chapterEntries.size else book.totalChapters
        val updatedBook = book.copy(totalPages = finalTotalPages, totalChapters = finalTotalChapters)

        bookDao.insertBook(BookEntity.fromDomain(updatedBook))

        val chapters = if (chapterEntries.isNotEmpty()) {
            chapterEntries.mapIndexed { index, entry ->
                ChapterEntity(
                    id = UUID.randomUUID().toString(),
                    bookId = updatedBook.id,
                    number = index + 1,
                    customTitle = entry.title,
                    pageCount = entry.pageCount,
                    isRead = false,
                    dateRead = null,
                    readingOrderIndex = null,
                    notes = "",
                    highlightedQuotes = "",
                    rating = 0f,
                    isPinnedNext = (index == 0) // pin first by default
                )
            }
        } else {
            (1..updatedBook.totalChapters).map { number ->
                ChapterEntity(
                    id = UUID.randomUUID().toString(),
                    bookId = updatedBook.id,
                    number = number,
                    customTitle = "",
                    pageCount = 0,
                    isRead = false,
                    dateRead = null,
                    readingOrderIndex = null,
                    notes = "",
                    highlightedQuotes = "",
                    rating = 0f,
                    isPinnedNext = (number == 1)
                )
            }
        }

        chapterDao.insertChapters(chapters)
    }

    suspend fun updateBook(book: Book) {
        bookDao.updateBook(BookEntity.fromDomain(book))
    }

    suspend fun deleteBook(bookId: String) {
        chapterDao.deleteChaptersByBookId(bookId)
        bookDao.deleteBookById(bookId)
    }

    suspend fun toggleChapterRead(bookId: String, chapterId: String) {
        val chapter = chapterDao.getChapterById(chapterId) ?: return
        val willBeRead = !chapter.isRead

        if (willBeRead) {
            val currentMaxOrder = chapterDao.getMaxReadingOrderIndex(bookId) ?: 0
            val newOrder = currentMaxOrder + 1
            chapterDao.updateReadStatus(
                chapterId = chapterId,
                isRead = true,
                dateRead = System.currentTimeMillis(),
                orderIndex = newOrder
            )
            // If it was pinned next, unpin it
            if (chapter.isPinnedNext) {
                chapterDao.updatePinned(chapterId, false)
                bookDao.updatePinnedChapter(bookId, null)
            }
        } else {
            chapterDao.updateReadStatus(
                chapterId = chapterId,
                isRead = false,
                dateRead = null,
                orderIndex = null
            )
        }

        // Auto-update book status
        val allChapters = chapterDao.getChaptersForBookOnce(bookId)
        val readCount = allChapters.count { it.isRead }
        val total = allChapters.size

        val currentBook = bookDao.getBookByIdOnce(bookId)
        if (currentBook != null) {
            if (readCount == total && total > 0) {
                bookDao.updateStatus(bookId, BookStatus.FINISHED.name)
            } else if (readCount > 0 && currentBook.status == BookStatus.WANT_TO_READ.name) {
                bookDao.updateStatus(bookId, BookStatus.READING.name)
            }
        }
    }

    suspend fun pinChapterNext(bookId: String, chapterId: String) {
        chapterDao.clearPinnedForBook(bookId)
        chapterDao.updatePinned(chapterId, true)
        bookDao.updatePinnedChapter(bookId, chapterId)
    }

    suspend fun unpinChapter(bookId: String, chapterId: String) {
        chapterDao.updatePinned(chapterId, false)
        bookDao.updatePinnedChapter(bookId, null)
    }

    suspend fun updateChapterDetails(
        chapterId: String,
        customTitle: String,
        notes: String,
        quotes: List<String>,
        rating: Float,
        pageCount: Int = 0
    ) {
        val existing = chapterDao.getChapterById(chapterId) ?: return
        val updated = existing.copy(
            customTitle = customTitle,
            pageCount = pageCount,
            notes = notes,
            highlightedQuotes = quotes.joinToString(";;;"),
            rating = rating
        )
        chapterDao.updateChapter(updated)
    }

    suspend fun updateSavedQuote(
        chapterId: String,
        quoteIndex: Int,
        newQuoteText: String,
        newNotes: String? = null,
        newPageCount: Int? = null,
        targetChapterId: String? = null,
        isFavorite: Boolean? = null
    ) {
        val currentChapter = chapterDao.getChapterById(chapterId) ?: return
        val existingQuotes = if (currentChapter.highlightedQuotes.isBlank()) {
            mutableListOf()
        } else {
            currentChapter.highlightedQuotes.split(";;;").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        }

        val targetChId = if (!targetChapterId.isNullOrBlank()) targetChapterId else chapterId

        if (targetChId == chapterId) {
            if (quoteIndex in existingQuotes.indices) {
                if (newQuoteText.isNotBlank()) {
                    existingQuotes[quoteIndex] = newQuoteText.trim()
                } else {
                    existingQuotes.removeAt(quoteIndex)
                }
            } else if (newQuoteText.isNotBlank()) {
                existingQuotes.add(newQuoteText.trim())
            }

            val updatedChapter = currentChapter.copy(
                highlightedQuotes = existingQuotes.joinToString(";;;"),
                notes = newNotes ?: currentChapter.notes,
                pageCount = if (newPageCount != null && newPageCount >= 0) newPageCount else currentChapter.pageCount
            )
            chapterDao.updateChapter(updatedChapter)
        } else {
            if (quoteIndex in existingQuotes.indices) {
                existingQuotes.removeAt(quoteIndex)
            }
            val updatedOldChapter = currentChapter.copy(
                highlightedQuotes = existingQuotes.joinToString(";;;")
            )
            chapterDao.updateChapter(updatedOldChapter)

            val targetChapter = chapterDao.getChapterById(targetChId)
            if (targetChapter != null) {
                val targetQuotes = if (targetChapter.highlightedQuotes.isBlank()) {
                    mutableListOf()
                } else {
                    targetChapter.highlightedQuotes.split(";;;").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                }
                if (newQuoteText.isNotBlank()) {
                    targetQuotes.add(newQuoteText.trim())
                }
                val updatedTargetChapter = targetChapter.copy(
                    highlightedQuotes = targetQuotes.joinToString(";;;"),
                    notes = newNotes ?: targetChapter.notes,
                    pageCount = if (newPageCount != null && newPageCount >= 0) newPageCount else targetChapter.pageCount
                )
                chapterDao.updateChapter(updatedTargetChapter)
            }
        }

        if (isFavorite != null) {
            val book = bookDao.getBookByIdOnce(currentChapter.bookId)
            if (book != null && book.isFavorite != isFavorite) {
                bookDao.updateFavorite(book.id, isFavorite)
            }
        }
    }

    suspend fun bulkUpdateChapterTitles(bookId: String, titles: List<String>) {
        val parsedList = titles.mapNotNull { com.example.domain.ChapterParser.parseLine(it) }
        bulkUpdateChapters(bookId, parsedList)
    }

    suspend fun bulkUpdateChapters(bookId: String, entries: List<com.example.domain.ParsedChapter>) {
        val existing = chapterDao.getChaptersForBookOnce(bookId).sortedBy { it.number }
        val updatedList = mutableListOf<ChapterEntity>()

        for (i in entries.indices) {
            val entry = entries[i]
            val title = entry.title.trim()
            val pageCount = entry.pageCount

            if (i < existing.size) {
                val current = existing[i]
                updatedList.add(
                    current.copy(
                        customTitle = title,
                        pageCount = if (pageCount > 0) pageCount else current.pageCount
                    )
                )
            } else {
                // Add new chapter
                updatedList.add(
                    ChapterEntity(
                        id = UUID.randomUUID().toString(),
                        bookId = bookId,
                        number = i + 1,
                        customTitle = title,
                        pageCount = pageCount,
                        isRead = false,
                        dateRead = null,
                        readingOrderIndex = null,
                        notes = "",
                        highlightedQuotes = "",
                        rating = 0f,
                        isPinnedNext = false
                    )
                )
            }
        }
        chapterDao.insertChapters(updatedList)

        // Update total chapters and total pages on book if appropriate
        val book = bookDao.getBookByIdOnce(bookId)
        if (book != null) {
            val chapterPagesSum = updatedList.sumOf { it.pageCount }
            val newTotalPages = if (book.totalPages > 0 && book.totalPages >= chapterPagesSum) {
                book.totalPages
            } else if (chapterPagesSum > 0) {
                chapterPagesSum
            } else {
                book.totalPages
            }
            val newTotalChapters = updatedList.size

            if (newTotalChapters != book.totalChapters || newTotalPages != book.totalPages) {
                bookDao.updateReadingProgress(bookId, newTotalChapters, newTotalPages)
            }
        }
    }

    suspend fun toggleFavorite(bookId: String, isFavorite: Boolean) {
        bookDao.updateFavorite(bookId, isFavorite)
    }

    suspend fun updateBookRating(bookId: String, rating: Float) {
        bookDao.updateRating(bookId, rating)
    }

    suspend fun updateBookStatus(bookId: String, status: BookStatus) {
        bookDao.updateStatus(bookId, status.name)
    }

    suspend fun updateCurrentPage(bookId: String, currentPage: Int) {
        bookDao.updateCurrentPage(bookId, currentPage)
    }

    suspend fun updateTotalPages(bookId: String, totalPages: Int) {
        bookDao.updateTotalPages(bookId, totalPages)
    }

    suspend fun createShelf(name: String, description: String = "", colorHex: String = "#F59E0B") {
        shelfDao.insertShelf(
            ShelfEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                colorHex = colorHex
            )
        )
    }

    suspend fun deleteShelf(shelfId: String) {
        shelfDao.deleteShelfById(shelfId)
    }

    private fun com.example.data.local.dao.BookWithProgress.toDomain(): Book {
        val domainStatus = BookStatus.fromString(status)
        val domainFormat = BookFormat.fromString(format)
        val genreList = if (genres.isBlank()) emptyList() else genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val shelfList = if (customShelves.isBlank()) emptyList() else customShelves.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        return Book(
            id = id,
            title = title,
            author = author,
            isbn = isbn,
            coverImageUrl = coverImageUrl,
            totalChapters = totalChapters,
            totalPages = totalPages,
            currentPage = currentPage,
            genres = genreList,
            seriesName = seriesName,
            seriesPosition = seriesPosition,
            status = domainStatus,
            format = domainFormat,
            rating = rating,
            dateAdded = dateAdded,
            dateStarted = dateStarted,
            dateFinished = dateFinished,
            isFavorite = isFavorite,
            pinnedNextChapterId = pinnedNextChapterId,
            customShelves = shelfList,
            completedChaptersCount = completedChaptersCount,
            completedChapterPages = completedChapterPages,
            totalChapterPages = totalChapterPages
        )
    }
}
