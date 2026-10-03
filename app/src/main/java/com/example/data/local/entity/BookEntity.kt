package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Book
import com.example.model.BookFormat
import com.example.model.BookStatus

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val coverImageUrl: String?,
    val totalChapters: Int,
    val totalPages: Int,
    val currentPage: Int = 0,
    val genres: String, // Comma-separated or JSON
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
    val customShelves: String // Comma-separated
) {
    fun toDomain(completedChaptersCount: Int = 0): Book {
        return Book(
            id = id,
            title = title,
            author = author,
            isbn = isbn,
            coverImageUrl = coverImageUrl,
            totalChapters = totalChapters,
            totalPages = totalPages,
            currentPage = currentPage,
            genres = if (genres.isBlank()) emptyList() else genres.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            seriesName = seriesName,
            seriesPosition = seriesPosition,
            status = BookStatus.fromString(status),
            format = BookFormat.fromString(format),
            rating = rating,
            dateAdded = dateAdded,
            dateStarted = dateStarted,
            dateFinished = dateFinished,
            isFavorite = isFavorite,
            pinnedNextChapterId = pinnedNextChapterId,
            customShelves = if (customShelves.isBlank()) emptyList() else customShelves.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            completedChaptersCount = completedChaptersCount
        )
    }

    companion object {
        fun fromDomain(book: Book): BookEntity {
            return BookEntity(
                id = book.id,
                title = book.title,
                author = book.author,
                isbn = book.isbn,
                coverImageUrl = book.coverImageUrl,
                totalChapters = book.totalChapters,
                totalPages = book.totalPages,
                currentPage = book.currentPage,
                genres = book.genres.joinToString(","),
                seriesName = book.seriesName,
                seriesPosition = book.seriesPosition,
                status = book.status.name,
                format = book.format.name,
                rating = book.rating,
                dateAdded = book.dateAdded,
                dateStarted = book.dateStarted,
                dateFinished = book.dateFinished,
                isFavorite = book.isFavorite,
                pinnedNextChapterId = book.pinnedNextChapterId,
                customShelves = book.customShelves.joinToString(",")
            )
        }
    }
}
