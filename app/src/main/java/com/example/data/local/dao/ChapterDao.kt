package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY number ASC")
    fun getChaptersForBook(bookId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY number ASC")
    suspend fun getChaptersForBookOnce(bookId: String): List<ChapterEntity>

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId AND isRead = 1 AND readingOrderIndex IS NOT NULL 
        ORDER BY readingOrderIndex ASC
    """)
    fun getReadingOrderTimeline(bookId: String): Flow<List<ChapterEntity>>

    @Query("SELECT MAX(readingOrderIndex) FROM chapters WHERE bookId = :bookId AND isRead = 1")
    suspend fun getMaxReadingOrderIndex(bookId: String): Int?

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersByBookId(bookId: String)

    @Query("""
        UPDATE chapters 
        SET isRead = :isRead, dateRead = :dateRead, readingOrderIndex = :orderIndex 
        WHERE id = :chapterId
    """)
    suspend fun updateReadStatus(chapterId: String, isRead: Boolean, dateRead: Long?, orderIndex: Int?)

    @Query("UPDATE chapters SET isPinnedNext = :isPinned WHERE id = :chapterId")
    suspend fun updatePinned(chapterId: String, isPinned: Boolean)

    @Query("UPDATE chapters SET isPinnedNext = 0 WHERE bookId = :bookId")
    suspend fun clearPinnedForBook(bookId: String)

    @Query("SELECT * FROM chapters WHERE highlightedQuotes != '' OR notes != '' ORDER BY dateRead DESC, number ASC")
    fun getAllNotesAndQuotes(): Flow<List<ChapterEntity>>

    @Query("SELECT COUNT(*) FROM chapters WHERE isRead = 1")
    fun getTotalCompletedChaptersCount(): Flow<Int>

    @Query("SELECT * FROM chapters WHERE isRead = 1 AND dateRead >= :sinceDateMillis")
    suspend fun getChaptersReadSince(sinceDateMillis: Long): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE isRead = 1")
    suspend fun getAllReadChapters(): List<ChapterEntity>
}
