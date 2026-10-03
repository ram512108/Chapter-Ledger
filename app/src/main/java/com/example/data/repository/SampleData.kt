package com.example.data.repository

import com.example.data.local.dao.BookDao
import com.example.data.local.dao.ChapterDao
import com.example.data.local.dao.ReadingSessionDao
import com.example.data.local.dao.ShelfDao
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.ReadingSessionEntity
import com.example.data.local.entity.ShelfEntity
import com.example.model.BookFormat
import com.example.model.BookStatus
import java.util.UUID

object SampleData {

    suspend fun seedDatabaseIfEmpty(
        bookDao: BookDao,
        chapterDao: ChapterDao,
        sessionDao: ReadingSessionDao,
        shelfDao: ShelfDao,
        preferencesRepository: PreferencesRepository
    ) {
        val shelves = listOf(
            ShelfEntity("shelf_favorites", "Favorites", "My all-time favorite reads", "#D97706"),
            ShelfEntity("shelf_scifi", "Sci-Fi & Fantasy", "Speculative fiction & space operas", "#3B82F6"),
            ShelfEntity("shelf_nonfiction", "Self & Craft", "Essays, systems, and nonfiction", "#10B981"),
            ShelfEntity("shelf_anthologies", "Anthologies", "Short fiction read in any order", "#8B5CF6")
        )
        shelves.forEach { shelfDao.insertShelf(it) }

        val exhalationId = "book_exhalation"
        val duneId = "book_dune"
        val hailMaryId = "book_hail_mary"
        val atomicHabitsId = "book_atomic_habits"
        val tomorrowId = "book_tomorrow"

        val now = System.currentTimeMillis()
        val oneDay = 86400000L

        // 1. Exhalation by Ted Chiang (Classic non-linear reading showcase)
        val exhalation = BookEntity(
            id = exhalationId,
            title = "Exhalation: Stories",
            author = "Ted Chiang",
            isbn = "9781101947883",
            totalChapters = 9,
            totalPages = 352,
            genres = "Science Fiction,Anthology,Philosophy",
            seriesName = null,
            seriesPosition = null,
            status = BookStatus.READING.name,
            format = BookFormat.PHYSICAL.name,
            rating = 4.8f,
            dateAdded = now - (30 * oneDay),
            dateStarted = now - (25 * oneDay),
            dateFinished = null,
            isFavorite = true,
            pinnedNextChapterId = "exhalation_ch_6",
            customShelves = "shelf_favorites,shelf_scifi,shelf_anthologies"
        )

        val exhalationStoryTitles = listOf(
            "The Merchant and the Alchemist's Gate",
            "Exhalation",
            "What's Expected of Us",
            "The Lifecycle of Software Objects",
            "Dacey's Patent Automatic Nanny",
            "The Truth of Fact, the Truth of Feeling",
            "The Great Silence",
            "Omphalos",
            "Anxiety Is the Dizziness of Freedom"
        )

        // Reading out of order:
        // Completed Story #1 first (orderIndex 1), Story #5 second (orderIndex 2), Story #8 third (orderIndex 3), Story #3 fourth (orderIndex 4)
        // Story #6 pinned next!
        val exhalationChapters = exhalationStoryTitles.mapIndexed { index, title ->
            val num = index + 1
            val chId = "exhalation_ch_$num"
            when (num) {
                1 -> ChapterEntity(
                    id = chId,
                    bookId = exhalationId,
                    number = num,
                    customTitle = title,
                    isRead = true,
                    dateRead = now - (20 * oneDay),
                    readingOrderIndex = 1,
                    notes = "Brilliant time travel narrative set in ancient Baghdad. Past and future cannot be changed, but our understanding can.",
                    highlightedQuotes = "Past and future are the same, and we cannot change either, only know them more fully.;;;Coincidence and intention are two sides of the same tapestry.",
                    rating = 5.0f,
                    isPinnedNext = false
                )
                5 -> ChapterEntity(
                    id = chId,
                    bookId = exhalationId,
                    number = num,
                    customTitle = title,
                    isRead = true,
                    dateRead = now - (14 * oneDay),
                    readingOrderIndex = 2,
                    notes = "Fascinating exploration of Victorian childcare technology vs human affection.",
                    highlightedQuotes = "A machine cannot bestow love, but it can mirror our own neglect.",
                    rating = 4.5f,
                    isPinnedNext = false
                )
                8 -> ChapterEntity(
                    id = chId,
                    bookId = exhalationId,
                    number = num,
                    customTitle = title,
                    isRead = true,
                    dateRead = now - (9 * oneDay),
                    readingOrderIndex = 3,
                    notes = "Creationism treated with strict scientific rigor. The realization of cosmological loneliness.",
                    highlightedQuotes = "Even if humanity is not the center of the universe, our search for truth remains sacred.",
                    rating = 5.0f,
                    isPinnedNext = false
                )
                3 -> ChapterEntity(
                    id = chId,
                    bookId = exhalationId,
                    number = num,
                    customTitle = title,
                    isRead = true,
                    dateRead = now - (3 * oneDay),
                    readingOrderIndex = 4,
                    notes = "A short story on free will and the predictor device that breaks human motivation.",
                    highlightedQuotes = "Pretend that you have free will. It's essential that you behave as if your decisions matter.",
                    rating = 4.5f,
                    isPinnedNext = false
                )
                6 -> ChapterEntity(
                    id = chId,
                    bookId = exhalationId,
                    number = num,
                    customTitle = title,
                    isRead = false,
                    dateRead = null,
                    readingOrderIndex = null,
                    notes = "",
                    highlightedQuotes = "",
                    rating = 0f,
                    isPinnedNext = true // Pinned next!
                )
                else -> ChapterEntity(
                    id = chId,
                    bookId = exhalationId,
                    number = num,
                    customTitle = title,
                    isRead = false,
                    dateRead = null,
                    readingOrderIndex = null,
                    notes = "",
                    highlightedQuotes = "",
                    rating = 0f,
                    isPinnedNext = false
                )
            }
        }

        // 2. Dune by Frank Herbert
        val dune = BookEntity(
            id = duneId,
            title = "Dune",
            author = "Frank Herbert",
            isbn = "9780441013593",
            totalChapters = 22,
            totalPages = 688,
            genres = "Science Fiction,Space Opera,Classics",
            seriesName = "Dune Chronicles",
            seriesPosition = 1.0f,
            status = BookStatus.READING.name,
            format = BookFormat.PHYSICAL.name,
            rating = 5.0f,
            dateAdded = now - (45 * oneDay),
            dateStarted = now - (40 * oneDay),
            dateFinished = null,
            isFavorite = true,
            pinnedNextChapterId = "dune_ch_9",
            customShelves = "shelf_favorites,shelf_scifi"
        )
        val duneChapters = (1..22).map { num ->
            val chId = "dune_ch_$num"
            val isRead = num <= 8
            ChapterEntity(
                id = chId,
                bookId = duneId,
                number = num,
                customTitle = if (num == 1) "A Beginning is a Very Delicate Time" else "Chapter $num",
                isRead = isRead,
                dateRead = if (isRead) now - ((40 - num * 3) * oneDay) else null,
                readingOrderIndex = if (isRead) num else null,
                notes = if (num == 1) "The Litany Against Fear introduced. Paul tested with the Gom Jabbar." else "",
                highlightedQuotes = if (num == 1) "I must not fear. Fear is the mind-killer. Fear is the little-death that brings total obliteration." else "",
                rating = if (num == 1) 5.0f else 0f,
                isPinnedNext = (num == 9)
            )
        }

        // 3. Project Hail Mary
        val hailMary = BookEntity(
            id = hailMaryId,
            title = "Project Hail Mary",
            author = "Andy Weir",
            isbn = "9780593135204",
            totalChapters = 30,
            totalPages = 496,
            genres = "Science Fiction,Humor,Space",
            seriesName = null,
            seriesPosition = null,
            status = BookStatus.READING.name,
            format = BookFormat.AUDIOBOOK.name,
            rating = 4.7f,
            dateAdded = now - (20 * oneDay),
            dateStarted = now - (18 * oneDay),
            dateFinished = null,
            isFavorite = true,
            pinnedNextChapterId = "hailmary_ch_22",
            customShelves = "shelf_scifi"
        )
        val hailMaryChapters = (1..30).map { num ->
            val chId = "hailmary_ch_$num"
            val isRead = num <= 21
            ChapterEntity(
                id = chId,
                bookId = hailMaryId,
                number = num,
                customTitle = "Chapter $num",
                isRead = isRead,
                dateRead = if (isRead) now - ((25 - num) * oneDay) else null,
                readingOrderIndex = if (isRead) num else null,
                notes = if (num == 12) "First contact made! Rocky is adorable." else "",
                highlightedQuotes = if (num == 12) "Amaze! Amaze! Amaze!;;;You are leaky space blob." else "",
                rating = if (num == 12) 5.0f else 0f,
                isPinnedNext = (num == 22)
            )
        }

        // 4. Atomic Habits
        val atomicHabits = BookEntity(
            id = atomicHabitsId,
            title = "Atomic Habits",
            author = "James Clear",
            isbn = "9780735211292",
            totalChapters = 20,
            totalPages = 320,
            genres = "Self Help,Psychology,Productivity",
            seriesName = null,
            seriesPosition = null,
            status = BookStatus.FINISHED.name,
            format = BookFormat.EBOOK.name,
            rating = 4.9f,
            dateAdded = now - (60 * oneDay),
            dateStarted = now - (55 * oneDay),
            dateFinished = now - (15 * oneDay),
            isFavorite = true,
            pinnedNextChapterId = null,
            customShelves = "shelf_favorites,shelf_nonfiction"
        )
        val atomicHabitsChapters = (1..20).map { num ->
            val chId = "atomic_ch_$num"
            ChapterEntity(
                id = chId,
                bookId = atomicHabitsId,
                number = num,
                customTitle = when (num) {
                    1 -> "The Surprising Power of Atomic Habits"
                    2 -> "How Your Habits Shape Your Identity"
                    3 -> "How to Build Better Habits in 4 Steps"
                    else -> "Chapter $num"
                },
                isRead = true,
                dateRead = now - ((55 - num * 2) * oneDay),
                readingOrderIndex = num,
                notes = if (num == 1) "Focus on systems instead of goals. 1% better every single day." else "",
                highlightedQuotes = if (num == 1) "You do not rise to the level of your goals. You fall to the level of your systems." else "",
                rating = 5.0f,
                isPinnedNext = false
            )
        }

        // 5. Tomorrow, and Tomorrow, and Tomorrow
        val tomorrow = BookEntity(
            id = tomorrowId,
            title = "Tomorrow, and Tomorrow, and Tomorrow",
            author = "Gabrielle Zevin",
            isbn = "9780593321201",
            totalChapters = 10,
            totalPages = 416,
            genres = "Literary Fiction,Video Games,Contemporary",
            seriesName = null,
            seriesPosition = null,
            status = BookStatus.WANT_TO_READ.name,
            format = BookFormat.PHYSICAL.name,
            rating = 0f,
            dateAdded = now - (5 * oneDay),
            dateStarted = null,
            dateFinished = null,
            isFavorite = false,
            pinnedNextChapterId = "tomorrow_ch_1",
            customShelves = ""
        )
        val tomorrowChapters = (1..10).map { num ->
            val chId = "tomorrow_ch_$num"
            ChapterEntity(
                id = chId,
                bookId = tomorrowId,
                number = num,
                customTitle = "Part $num",
                isRead = false,
                dateRead = null,
                readingOrderIndex = null,
                notes = "",
                highlightedQuotes = "",
                rating = 0f,
                isPinnedNext = (num == 1)
            )
        }

        val allBooks = listOf(exhalation, dune, hailMary, atomicHabits, tomorrow)
        val allChapters = exhalationChapters + duneChapters + hailMaryChapters + atomicHabitsChapters + tomorrowChapters

        bookDao.insertBooks(allBooks)
        chapterDao.insertChapters(allChapters)

        // Reading Sessions
        val sampleSessions = listOf(
            ReadingSessionEntity(
                id = UUID.randomUUID().toString(),
                bookId = exhalationId,
                bookTitle = "Exhalation: Stories",
                chapterNumbersCovered = "3",
                startTime = now - (3 * oneDay + 7200000),
                endTime = now - (3 * oneDay),
                durationMinutes = 45,
                pagesCovered = 35,
                notes = "Finished 'What's Expected of Us'. Quick and deeply thought provoking."
            ),
            ReadingSessionEntity(
                id = UUID.randomUUID().toString(),
                bookId = hailMaryId,
                bookTitle = "Project Hail Mary",
                chapterNumbersCovered = "20, 21",
                startTime = now - (2 * oneDay + 3600000),
                endTime = now - (2 * oneDay),
                durationMinutes = 60,
                pagesCovered = 40,
                notes = "Fascinating science problem solving with the xenonite chain."
            ),
            ReadingSessionEntity(
                id = UUID.randomUUID().toString(),
                bookId = duneId,
                bookTitle = "Dune",
                chapterNumbersCovered = "8",
                startTime = now - (1 * oneDay + 5400000),
                endTime = now - (1 * oneDay),
                durationMinutes = 50,
                pagesCovered = 32,
                notes = "Jessica and Paul's escape into the desert storms."
            ),
            ReadingSessionEntity(
                id = UUID.randomUUID().toString(),
                bookId = exhalationId,
                bookTitle = "Exhalation: Stories",
                chapterNumbersCovered = "8",
                startTime = now - (9 * oneDay + 4800000),
                endTime = now - (9 * oneDay),
                durationMinutes = 40,
                pagesCovered = 28,
                notes = "Read Omphalos in the afternoon."
            )
        )
        sampleSessions.forEach { sessionDao.insertSession(it) }

        preferencesRepository.setSeedDataInitialized(true)
    }
}
