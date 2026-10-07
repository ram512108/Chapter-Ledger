package com.example

import android.app.Application
import com.example.data.repository.SampleData
import com.example.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChapterlyApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Seed initial rich sample data if first time
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isInitialized = container.preferencesRepository.isSeedDataInitialized.first()
                if (!isInitialized) {
                    SampleData.seedDatabaseIfEmpty(
                        bookDao = container.database.bookDao(),
                        chapterDao = container.database.chapterDao(),
                        sessionDao = container.database.readingSessionDao(),
                        shelfDao = container.database.shelfDao(),
                        preferencesRepository = container.preferencesRepository
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
