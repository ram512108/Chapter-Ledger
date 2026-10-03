package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.BookWithChapters
import kotlinx.coroutines.flow.Flow

data class BookWithProgress(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val coverImageUrl: String?,
    val totalChapters: Int,
    val totalPages: Int,
    val currentPage: Int = 0,
    val genres: String,
    val seriesName: String?,
    val seriesPosition: Float?,
    val status: String,
    val format: String,
    val rating: Float,
    val dateAdded: Long,
    val dateStarted: Long?,
    val dateFinished: Long?,
    val isFavorite: Boolean,
    val pinnedNextChapterId: String?,
    val customShelves: String,
    val completedChaptersCount: Int,
    val completedChapterPages: Int = 0,
    val totalChapterPages: Int = 0
)

@Dao
interface BookDao {
    @Query("""
        SELECT b.*, 
               COALESCE((SELECT COUNT(*) FROM chapters c WHERE c.bookId = b.id AND c.isRead = 1), 0) as completedChaptersCount,
               COALESCE((SELECT SUM(c.pageCount) FROM chapters c WHERE c.bookId = b.id AND c.isRead = 1), 0) as completedChapterPages,
               COALESCE((SELECT SUM(c.pageCount) FROM chapters c WHERE c.bookId = b.id), 0) as totalChapterPages
        FROM books b
        ORDER BY b.dateAdded DESC
    """)
    fun getAllBooksWithProgress(): Flow<List<BookWithProgress>>

    @Query("""
        SELECT b.*, 
               COALESCE((SELECT COUNT(*) FROM chapters c WHERE c.bookId = b.id AND c.isRead = 1), 0) as completedChaptersCount,
               COALESCE((SELECT SUM(c.pageCount) FROM chapters c WHERE c.bookId = b.id AND c.isRead = 1), 0) as completedChapterPages,
               COALESCE((SELECT SUM(c.pageCount) FROM chapters c WHERE c.bookId = b.id), 0) as totalChapterPages
        FROM books b
        WHERE b.id = :bookId
    """)
    fun getBookById(bookId: String): Flow<BookWithProgress?>

    @Query("""
        SELECT b.*, 
               COALESCE((SELECT COUNT(*) FROM chapters c WHERE c.bookId = b.id AND c.isRead = 1), 0) as completedChaptersCount,
               COALESCE((SELECT SUM(c.pageCount) FROM chapters c WHERE c.bookId = b.id AND c.isRead = 1), 0) as completedChapterPages,
               COALESCE((SELECT SUM(c.pageCount) FROM chapters c WHERE c.bookId = b.id), 0) as totalChapterPages
        FROM books b
        WHERE b.id = :bookId
    """)
    suspend fun getBookByIdOnce(bookId: String): BookWithProgress?

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookWithChapters(bookId: String): Flow<BookWithChapters?>

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookWithChaptersOnce(bookId: String): BookWithChapters?

    @Transaction
    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun getAllBooksWithChapters(): Flow<List<BookWithChapters>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: String)

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :bookId")
    suspend fun updateFavorite(bookId: String, isFavorite: Boolean)

    @Query("UPDATE books SET pinnedNextChapterId = :chapterId WHERE id = :bookId")
    suspend fun updatePinnedChapter(bookId: String, chapterId: String?)

    @Query("UPDATE books SET status = :status WHERE id = :bookId")
    suspend fun updateStatus(bookId: String, status: String)

    @Query("UPDATE books SET rating = :rating WHERE id = :bookId")
    suspend fun updateRating(bookId: String, rating: Float)

    @Query("UPDATE books SET totalChapters = :totalChapters, totalPages = :totalPages WHERE id = :bookId")
    suspend fun updateReadingProgress(bookId: String, totalChapters: Int, totalPages: Int)

    @Query("UPDATE books SET currentPage = :currentPage WHERE id = :bookId")
    suspend fun updateCurrentPage(bookId: String, currentPage: Int)

    @Query("UPDATE books SET totalPages = :totalPages WHERE id = :bookId")
    suspend fun updateTotalPages(bookId: String, totalPages: Int)
}
