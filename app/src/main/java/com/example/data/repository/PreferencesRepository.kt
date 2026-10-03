package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ActiveReadingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "chapter_ledger_prefs")

class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_GRID_MODE = booleanPreferencesKey("is_grid_mode")
        val SELECTED_SHELF = stringPreferencesKey("selected_shelf")
        val ANNUAL_BOOK_GOAL = intPreferencesKey("annual_book_goal")
        val SEED_DATA_INITIALIZED = booleanPreferencesKey("seed_data_initialized")

        // Active Session Keys
        val ACTIVE_SESSION_ID = stringPreferencesKey("active_session_id")
        val ACTIVE_SESSION_BOOK_ID = stringPreferencesKey("active_session_book_id")
        val ACTIVE_SESSION_BOOK_TITLE = stringPreferencesKey("active_session_book_title")
        val ACTIVE_SESSION_START_TIME = longPreferencesKey("active_session_start_time")
        val ACTIVE_SESSION_LAST_RESUME_TIME = longPreferencesKey("active_session_last_resume_time")
        val ACTIVE_SESSION_ACCUMULATED_SECONDS = longPreferencesKey("active_session_accumulated_seconds")
        val ACTIVE_SESSION_IS_RUNNING = booleanPreferencesKey("active_session_is_running")
        val ACTIVE_SESSION_CHAPTERS = stringPreferencesKey("active_session_chapters")
        val ACTIVE_SESSION_PAGES = stringPreferencesKey("active_session_pages")
        val ACTIVE_SESSION_NOTES = stringPreferencesKey("active_session_notes")
    }

    val isGridMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.IS_GRID_MODE] ?: true
    }

    val annualBookGoal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ANNUAL_BOOK_GOAL] ?: 24
    }

    val isSeedDataInitialized: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SEED_DATA_INITIALIZED] ?: false
    }

    val activeReadingSession: Flow<ActiveReadingSession?> = context.dataStore.data.map { prefs ->
        val id = prefs[PreferencesKeys.ACTIVE_SESSION_ID]
        val bookId = prefs[PreferencesKeys.ACTIVE_SESSION_BOOK_ID]
        if (id.isNullOrBlank() || bookId.isNullOrBlank()) {
            null
        } else {
            ActiveReadingSession(
                id = id,
                bookId = bookId,
                bookTitle = prefs[PreferencesKeys.ACTIVE_SESSION_BOOK_TITLE] ?: "",
                startTime = prefs[PreferencesKeys.ACTIVE_SESSION_START_TIME] ?: System.currentTimeMillis(),
                lastResumeTime = prefs[PreferencesKeys.ACTIVE_SESSION_LAST_RESUME_TIME] ?: 0L,
                accumulatedSeconds = prefs[PreferencesKeys.ACTIVE_SESSION_ACCUMULATED_SECONDS] ?: 0L,
                isRunning = prefs[PreferencesKeys.ACTIVE_SESSION_IS_RUNNING] ?: false,
                chaptersCovered = prefs[PreferencesKeys.ACTIVE_SESSION_CHAPTERS] ?: "",
                pagesCovered = prefs[PreferencesKeys.ACTIVE_SESSION_PAGES] ?: "",
                notes = prefs[PreferencesKeys.ACTIVE_SESSION_NOTES] ?: ""
            )
        }
    }

    suspend fun setGridMode(isGrid: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_GRID_MODE] = isGrid
        }
    }

    suspend fun setAnnualBookGoal(target: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ANNUAL_BOOK_GOAL] = target
        }
    }

    suspend fun setSeedDataInitialized(initialized: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SEED_DATA_INITIALIZED] = initialized
        }
    }

    suspend fun getActiveReadingSessionOnce(): ActiveReadingSession? {
        return activeReadingSession.first()
    }

    suspend fun startOrGetActiveSession(bookId: String, bookTitle: String): ActiveReadingSession {
        val current = getActiveReadingSessionOnce()
        if (current != null && current.bookId == bookId) {
            return current
        }
        val now = System.currentTimeMillis()
        val newSession = ActiveReadingSession(
            id = UUID.randomUUID().toString(),
            bookId = bookId,
            bookTitle = bookTitle,
            startTime = now,
            lastResumeTime = now,
            accumulatedSeconds = 0L,
            isRunning = true,
            chaptersCovered = "",
            pagesCovered = "",
            notes = ""
        )
        saveActiveReadingSession(newSession)
        return newSession
    }

    suspend fun saveActiveReadingSession(session: ActiveReadingSession) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ACTIVE_SESSION_ID] = session.id
            prefs[PreferencesKeys.ACTIVE_SESSION_BOOK_ID] = session.bookId
            prefs[PreferencesKeys.ACTIVE_SESSION_BOOK_TITLE] = session.bookTitle
            prefs[PreferencesKeys.ACTIVE_SESSION_START_TIME] = session.startTime
            prefs[PreferencesKeys.ACTIVE_SESSION_LAST_RESUME_TIME] = session.lastResumeTime
            prefs[PreferencesKeys.ACTIVE_SESSION_ACCUMULATED_SECONDS] = session.accumulatedSeconds
            prefs[PreferencesKeys.ACTIVE_SESSION_IS_RUNNING] = session.isRunning
            prefs[PreferencesKeys.ACTIVE_SESSION_CHAPTERS] = session.chaptersCovered
            prefs[PreferencesKeys.ACTIVE_SESSION_PAGES] = session.pagesCovered
            prefs[PreferencesKeys.ACTIVE_SESSION_NOTES] = session.notes
        }
    }

    suspend fun updateActiveSessionDraft(chapters: String, pages: String, notes: String) {
        context.dataStore.edit { prefs ->
            if (prefs[PreferencesKeys.ACTIVE_SESSION_ID] != null) {
                prefs[PreferencesKeys.ACTIVE_SESSION_CHAPTERS] = chapters
                prefs[PreferencesKeys.ACTIVE_SESSION_PAGES] = pages
                prefs[PreferencesKeys.ACTIVE_SESSION_NOTES] = notes
            }
        }
    }

    suspend fun toggleActiveSessionTimer(): ActiveReadingSession? {
        val current = getActiveReadingSessionOnce() ?: return null
        val now = System.currentTimeMillis()
        val updated = if (current.isRunning) {
            val totalSeconds = current.calculateElapsedSeconds(now)
            current.copy(
                accumulatedSeconds = totalSeconds,
                lastResumeTime = 0L,
                isRunning = false
            )
        } else {
            current.copy(
                lastResumeTime = now,
                isRunning = true
            )
        }
        saveActiveReadingSession(updated)
        return updated
    }

    suspend fun clearActiveReadingSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.ACTIVE_SESSION_ID)
            prefs.remove(PreferencesKeys.ACTIVE_SESSION_BOOK_ID)
            prefs.remove(PreferencesKeys.ACTIVE_SESSION_BOOK_TITLE)
            prefs.remove(PreferencesKeys.ACTIVE_SESSION_START_TIME)
            prefs.remove(PreferencesKeys.ACTIVE_SESSION_LAST_RESUME_TIME)
            prefs.remove(PreferencesKeys.ACTIVE_SESSION_ACCUMULATED_SECONDS)
            prefs.remove(PreferencesKeys.ACTIVE_SESSION_IS_RUNNING)
            prefs.remove(PreferencesKeys.ACTIVE_SESSION_CHAPTERS)
            prefs.remove(PreferencesKeys.ACTIVE_SESSION_PAGES)
            prefs.remove(PreferencesKeys.ACTIVE_SESSION_NOTES)
        }
    }
}
