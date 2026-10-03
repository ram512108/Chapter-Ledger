package com.example.model

import java.util.UUID

enum class BookStatus(val displayName: String) {
    READING("Currently Reading"),
    WANT_TO_READ("Want to Read"),
    FINISHED("Finished"),
    ON_HOLD("On Hold"),
    DID_NOT_FINISH("Did Not Finish");

    companion object {
        fun fromString(value: String): BookStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: WANT_TO_READ
        }
    }
}

enum class BookFormat(val displayName: String) {
    PHYSICAL("Physical"),
    EBOOK("E-Book"),
    AUDIOBOOK("Audiobook");

    companion object {
        fun fromString(value: String): BookFormat {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: PHYSICAL
        }
    }
}

data class Book(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String,
    val isbn: String = "",
    val coverImageUrl: String? = null,
    val totalChapters: Int = 10,
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val genres: List<String> = emptyList(),
    val seriesName: String? = null,
    val seriesPosition: Float? = null,
    val status: BookStatus = BookStatus.READING,
    val format: BookFormat = BookFormat.PHYSICAL,
    val rating: Float = 0f, // 0.0 to 5.0
    val dateAdded: Long = System.currentTimeMillis(),
    val dateStarted: Long? = null,
    val dateFinished: Long? = null,
    val isFavorite: Boolean = false,
    val pinnedNextChapterId: String? = null,
    val customShelves: List<String> = emptyList(),
    // Transient / calculated fields populated with chapters
    val completedChaptersCount: Int = 0,
    val completedChapterPages: Int = 0,
    val totalChapterPages: Int = 0
) {
    val chapterProgressPercentage: Int
        get() = if (totalChapters > 0) ((completedChaptersCount * 100) / totalChapters).coerceIn(0, 100) else 0

    val pageProgressPercentage: Int
        get() = if (totalPages > 0) ((currentPage * 100) / totalPages).coerceIn(0, 100) else 0

    val chapterPagesProgressPercentage: Int
        get() = if (totalChapterPages > 0) ((completedChapterPages * 100) / totalChapterPages).coerceIn(0, 100) else 0

    val progressPercentage: Int
        get() = when {
            totalPages > 0 && currentPage > 0 -> pageProgressPercentage
            totalChapterPages > 0 && completedChapterPages > 0 -> chapterPagesProgressPercentage
            totalChapters > 0 && completedChaptersCount > 0 -> chapterProgressPercentage
            totalPages > 0 -> pageProgressPercentage
            totalChapterPages > 0 -> chapterPagesProgressPercentage
            totalChapters > 0 -> chapterProgressPercentage
            else -> 0
        }

    val isComplete: Boolean
        get() = (completedChaptersCount >= totalChapters && totalChapters > 0) || 
                (currentPage >= totalPages && totalPages > 0) ||
                (totalChapterPages > 0 && completedChapterPages >= totalChapterPages)
}
