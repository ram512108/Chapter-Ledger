package com.example.model

import java.util.UUID

data class Chapter(
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val number: Int,
    val customTitle: String = "",
    val pageCount: Int = 0,
    val isRead: Boolean = false,
    val dateRead: Long? = null,
    val readingOrderIndex: Int? = null, // sequence completed, e.g. 1st, 2nd, 3rd
    val notes: String = "",
    val highlightedQuotes: List<String> = emptyList(),
    val rating: Float = 0f, // 0.0 to 5.0
    val isPinnedNext: Boolean = false
) {
    val displayTitle: String
        get() = if (customTitle.isNotBlank()) customTitle else "Chapter $number"
}
