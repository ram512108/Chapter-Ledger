package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 1-to-many relationship data class representing a Book and its associated Chapters in Room.
 */
data class BookWithChapters(
    @Embedded
    val book: BookEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val chapters: List<ChapterEntity>
)
