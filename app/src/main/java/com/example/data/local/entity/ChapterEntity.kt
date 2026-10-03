package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.Chapter

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId"]),
        Index(value = ["bookId", "number"], unique = true)
    ]
)
data class ChapterEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val number: Int,
    val customTitle: String,
    val pageCount: Int = 0,
    val isRead: Boolean,
    val dateRead: Long?,
    val readingOrderIndex: Int?,
    val notes: String,
    val highlightedQuotes: String, // newline separated or empty
    val rating: Float,
    val isPinnedNext: Boolean
) {
    fun toDomain(): Chapter {
        return Chapter(
            id = id,
            bookId = bookId,
            number = number,
            customTitle = customTitle,
            pageCount = pageCount,
            isRead = isRead,
            dateRead = dateRead,
            readingOrderIndex = readingOrderIndex,
            notes = notes,
            highlightedQuotes = if (highlightedQuotes.isBlank()) emptyList() else highlightedQuotes.split(";;;").map { it.trim() }.filter { it.isNotEmpty() },
            rating = rating,
            isPinnedNext = isPinnedNext
        )
    }

    companion object {
        fun fromDomain(chapter: Chapter): ChapterEntity {
            return ChapterEntity(
                id = chapter.id,
                bookId = chapter.bookId,
                number = chapter.number,
                customTitle = chapter.customTitle,
                pageCount = chapter.pageCount,
                isRead = chapter.isRead,
                dateRead = chapter.dateRead,
                readingOrderIndex = chapter.readingOrderIndex,
                notes = chapter.notes,
                highlightedQuotes = chapter.highlightedQuotes.joinToString(";;;"),
                rating = chapter.rating,
                isPinnedNext = chapter.isPinnedNext
            )
        }
    }
}
