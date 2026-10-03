package com.example

import com.example.domain.ExportImportManager
import com.example.model.Book
import com.example.model.BookFormat
import com.example.model.BookStatus
import com.example.model.Chapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChapterLedgerTest {

    @Test
    fun `test non-linear chapter progress calculation`() {
        val book = Book(
            id = "test_1",
            title = "Exhalation",
            author = "Ted Chiang",
            totalChapters = 9,
            completedChaptersCount = 4
        )

        assertEquals(44, book.progressPercentage)
        assertEquals(false, book.isComplete)
    }

    @Test
    fun `test export to json preserves essential fields`() {
        val book = Book(
            id = "b1",
            title = "Dune",
            author = "Frank Herbert",
            totalChapters = 22,
            totalPages = 688,
            status = BookStatus.READING,
            format = BookFormat.PHYSICAL,
            genres = listOf("Sci-Fi", "Space Opera"),
            rating = 5.0f,
            isFavorite = true
        )

        val json = ExportImportManager.exportToJson(listOf(book))
        assertTrue(json.contains("\"title\": \"Dune\""))
        assertTrue(json.contains("\"author\": \"Frank Herbert\""))
        assertTrue(json.contains("\"totalChapters\": 22"))
        assertTrue(json.contains("\"isFavorite\": true"))
    }

    @Test
    fun `test chapter display title fallback`() {
        val ch1 = Chapter(
            id = "c1",
            bookId = "b1",
            number = 1,
            customTitle = ""
        )
        assertEquals("Chapter 1", ch1.displayTitle)

        val ch2 = Chapter(
            id = "c2",
            bookId = "b1",
            number = 2,
            customTitle = "The Merchant and the Alchemist's Gate"
        )
        assertEquals("The Merchant and the Alchemist's Gate", ch2.displayTitle)
    }

    @Test
    fun `test page tracking progress calculation`() {
        val book = Book(
            id = "b2",
            title = "Neuromancer",
            author = "William Gibson",
            totalChapters = 24,
            totalPages = 300,
            currentPage = 150
        )

        assertEquals(50, book.pageProgressPercentage)
        assertEquals(50, book.progressPercentage)
    }
}
