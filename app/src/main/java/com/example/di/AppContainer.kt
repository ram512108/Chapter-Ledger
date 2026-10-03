package com.example.di

import android.content.Context
import com.example.data.local.ChapterLedgerDatabase
import com.example.data.remote.OpenLibraryService
import com.example.data.repository.BookRepository
import com.example.data.repository.GoalRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.ReadingSessionRepository

class AppContainer(private val context: Context) {

    val database: ChapterLedgerDatabase by lazy {
        ChapterLedgerDatabase.getInstance(context)
    }

    val bookRepository: BookRepository by lazy {
        BookRepository(
            bookDao = database.bookDao(),
            chapterDao = database.chapterDao(),
            shelfDao = database.shelfDao()
        )
    }

    val readingSessionRepository: ReadingSessionRepository by lazy {
        ReadingSessionRepository(database.readingSessionDao())
    }

    val goalRepository: GoalRepository by lazy {
        GoalRepository(database.goalDao())
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(context)
    }

    val openLibraryService: OpenLibraryService by lazy {
        OpenLibraryService.create()
    }
}
