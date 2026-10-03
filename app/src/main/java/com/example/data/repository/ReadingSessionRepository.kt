package com.example.data.repository

import com.example.data.local.dao.ReadingSessionDao
import com.example.data.local.entity.ReadingSessionEntity
import com.example.model.ReadingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReadingSessionRepository(
    private val sessionDao: ReadingSessionDao
) {
    val allSessions: Flow<List<ReadingSession>> = sessionDao.getAllSessions().map { list ->
        list.map { it.toDomain() }
    }

    fun getSessionsForBook(bookId: String): Flow<List<ReadingSession>> =
        sessionDao.getSessionsForBook(bookId).map { list -> list.map { it.toDomain() } }

    suspend fun insertSession(session: ReadingSession) {
        sessionDao.insertSession(ReadingSessionEntity.fromDomain(session))
    }

    suspend fun deleteSession(id: String) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun deleteSessionsForBook(bookId: String) {
        sessionDao.deleteSessionsByBookId(bookId)
    }

    suspend fun getAllSessionsOnce(): List<ReadingSession> {
        return sessionDao.getAllSessionsOnce().map { it.toDomain() }
    }

    suspend fun getSessionsSince(sinceMillis: Long): List<ReadingSession> {
        return sessionDao.getSessionsSince(sinceMillis).map { it.toDomain() }
    }
}
