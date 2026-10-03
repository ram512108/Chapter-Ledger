package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BookDao
import com.example.data.local.dao.ChapterDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.ReadingSessionDao
import com.example.data.local.dao.ShelfDao
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.ReadingGoalEntity
import com.example.data.local.entity.ReadingSessionEntity
import com.example.data.local.entity.ShelfEntity

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        ReadingSessionEntity::class,
        ReadingGoalEntity::class,
        ShelfEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ChapterLedgerDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun goalDao(): GoalDao
    abstract fun shelfDao(): ShelfDao

    companion object {
        @Volatile
        private var INSTANCE: ChapterLedgerDatabase? = null

        fun getInstance(context: Context): ChapterLedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChapterLedgerDatabase::class.java,
                    "chapter_ledger_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
