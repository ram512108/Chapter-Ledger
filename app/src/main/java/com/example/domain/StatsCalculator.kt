package com.example.domain

import com.example.data.local.dao.ChapterDao
import com.example.data.local.dao.ReadingSessionDao
import com.example.model.Book
import com.example.model.BookFormat
import com.example.model.BookStatus
import com.example.model.HeatmapDay
import com.example.model.PaceEstimate
import com.example.model.ReadingStats
import java.util.Calendar
import java.util.concurrent.TimeUnit

object StatsCalculator {

    suspend fun calculateStats(
        books: List<Book>,
        chapterDao: ChapterDao,
        sessionDao: ReadingSessionDao
    ): ReadingStats {
        val allReadChapters = chapterDao.getAllReadChapters()
        val allSessions = sessionDao.getAllSessionsOnce()

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // Days calculation
        val todayStart = getStartOfDay(now)
        val oneDayMillis = TimeUnit.DAYS.toMillis(1)

        // Streak calculation
        val activityDays = mutableSetOf<Long>()
        allReadChapters.forEach { ch ->
            ch.dateRead?.let { activityDays.add(getStartOfDay(it)) }
        }
        allSessions.forEach { sess ->
            activityDays.add(getStartOfDay(sess.startTime))
        }

        var currentStreak = 0
        var checkDay = todayStart

        // If not read today, check if read yesterday to keep streak active
        if (!activityDays.contains(checkDay)) {
            checkDay -= oneDayMillis
        }

        while (activityDays.contains(checkDay)) {
            currentStreak++
            checkDay -= oneDayMillis
        }

        // Longest streak
        val sortedDays = activityDays.sorted()
        var longestStreak = 0
        var currentRun = 0
        var prevDay: Long? = null

        for (day in sortedDays) {
            if (prevDay == null || day == prevDay + oneDayMillis) {
                currentRun++
            } else {
                currentRun = 1
            }
            if (currentRun > longestStreak) {
                longestStreak = currentRun
            }
            prevDay = day
        }
        if (currentStreak > longestStreak) longestStreak = currentStreak

        // Time ranges
        val startOfWeek = getStartOfWeek(now)
        val startOfMonth = getStartOfMonth(now)
        val startOfYear = getStartOfYear(now)

        val chaptersThisWeek = allReadChapters.count { (it.dateRead ?: 0) >= startOfWeek }
        val chaptersThisMonth = allReadChapters.count { (it.dateRead ?: 0) >= startOfMonth }
        val chaptersThisYear = allReadChapters.count { (it.dateRead ?: 0) >= startOfYear }

        val booksThisYear = books.count { it.status == BookStatus.FINISHED && (it.dateFinished ?: it.dateAdded) >= startOfYear }
        val totalBooksFinished = books.count { it.status == BookStatus.FINISHED }

        val totalPagesRead = books.filter { it.status == BookStatus.FINISHED }.sumOf { it.totalPages }
        val totalReadingMinutes = allSessions.sumOf { it.durationMinutes }

        // Heatmap for past 70 days (10 weeks)
        val heatmapDays = mutableListOf<HeatmapDay>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = todayStart
        // Go back 70 days
        cal.add(Calendar.DAY_OF_YEAR, -69)

        for (i in 0 until 70) {
            val dayTime = cal.timeInMillis
            val dayEnd = dayTime + oneDayMillis - 1

            val chaptersOnDay = allReadChapters.count { ch ->
                val dr = ch.dateRead ?: 0
                dr in dayTime..dayEnd
            }
            val sessionsOnDay = allSessions.filter { it.startTime in dayTime..dayEnd }
            val minutesOnDay = sessionsOnDay.sumOf { it.durationMinutes }

            val level = when {
                chaptersOnDay >= 4 || minutesOnDay >= 60 -> 4
                chaptersOnDay >= 2 || minutesOnDay >= 40 -> 3
                chaptersOnDay >= 1 || minutesOnDay >= 20 -> 2
                chaptersOnDay > 0 || minutesOnDay > 0 -> 1
                else -> 0
            }

            heatmapDays.add(
                HeatmapDay(
                    dateMillis = dayTime,
                    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                    monthOfYear = cal.get(Calendar.MONTH),
                    year = cal.get(Calendar.YEAR),
                    chaptersCount = chaptersOnDay,
                    readingMinutes = minutesOnDay,
                    level = level
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Genre distribution
        val genreMap = mutableMapOf<String, Int>()
        books.forEach { b ->
            b.genres.forEach { g ->
                genreMap[g] = (genreMap[g] ?: 0) + 1
            }
        }

        // Format distribution
        val formatMap = mutableMapOf<BookFormat, Int>()
        books.forEach { b ->
            formatMap[b.format] = (formatMap[b.format] ?: 0) + 1
        }

        // Pace estimates for currently reading books
        // Average speed: chapters read in the last 30 days
        val thirtyDaysAgo = now - TimeUnit.DAYS.toMillis(30)
        val recentChaptersRead = allReadChapters.count { (it.dateRead ?: 0) >= thirtyDaysAgo }
        val avgChaptersPerDay = if (recentChaptersRead > 0) (recentChaptersRead.toFloat() / 30f).coerceAtLeast(0.1f) else 0.35f

        val paceEstimates = books.filter { it.status == BookStatus.READING }.map { book ->
            val remaining = (book.totalChapters - book.completedChaptersCount).coerceAtLeast(1)
            val daysNeeded = (remaining / avgChaptersPerDay).toInt().coerceAtLeast(1)
            val finishDate = now + TimeUnit.DAYS.toMillis(daysNeeded.toLong())

            PaceEstimate(
                bookId = book.id,
                bookTitle = book.title,
                remainingChapters = remaining,
                estimatedDaysToFinish = daysNeeded,
                estimatedFinishDateMillis = finishDate
            )
        }

        return ReadingStats(
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            totalChaptersRead = allReadChapters.size,
            totalBooksFinished = totalBooksFinished,
            totalPagesRead = totalPagesRead,
            totalReadingMinutes = totalReadingMinutes,
            chaptersThisWeek = chaptersThisWeek,
            chaptersThisMonth = chaptersThisMonth,
            chaptersThisYear = chaptersThisYear,
            pagesThisYear = totalPagesRead,
            booksThisYear = booksThisYear,
            paceEstimates = paceEstimates,
            heatmapDays = heatmapDays,
            genreDistribution = genreMap,
            formatDistribution = formatMap
        )
    }

    private fun getStartOfDay(timeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getStartOfWeek(timeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getStartOfMonth(timeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getStartOfYear(timeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
