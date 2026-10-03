package com.example.domain

import com.example.model.Chapter

data class ParsedChapter(
    val title: String,
    val pageCount: Int = 0,
    val rawLine: String = "",
    val hasError: Boolean = false,
    val errorMessage: String? = null
)

object ChapterParser {

    /**
     * Parses a single line in the format "chapter_title|page_count" or "chapter_title".
     * E.g.:
     *   "Chapter 1|24"      -> title: "Chapter 1", pageCount: 24
     *   "Chapter 2 | 18 "   -> title: "Chapter 2", pageCount: 18
     *   "Prologue"          -> title: "Prologue", pageCount: 0
     *   "Chapter 3|"        -> title: "Chapter 3", pageCount: 0
     *   "Chapter 4|abc"     -> title: "Chapter 4", pageCount: 0, hasError: true
     */
    fun parseLine(line: String, defaultChapterNumber: Int? = null): ParsedChapter? {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return null

        return if (trimmed.contains("|")) {
            val parts = trimmed.split("|", limit = 2)
            val titlePart = parts[0].trim()
            val pagePart = parts.getOrNull(1)?.trim() ?: ""

            val title = if (titlePart.isNotBlank()) {
                titlePart
            } else if (defaultChapterNumber != null) {
                "Chapter $defaultChapterNumber"
            } else {
                ""
            }

            if (pagePart.isEmpty()) {
                ParsedChapter(
                    title = title,
                    pageCount = 0,
                    rawLine = line
                )
            } else {
                val parsedCount = pagePart.toIntOrNull()
                if (parsedCount != null && parsedCount >= 0) {
                    ParsedChapter(
                        title = title,
                        pageCount = parsedCount,
                        rawLine = line
                    )
                } else {
                    ParsedChapter(
                        title = title,
                        pageCount = 0,
                        rawLine = line,
                        hasError = true,
                        errorMessage = "Invalid page count '$pagePart'. Must be a positive number."
                    )
                }
            }
        } else {
            ParsedChapter(
                title = trimmed,
                pageCount = 0,
                rawLine = line
            )
        }
    }

    /**
     * Parses multiple lines of chapter entries.
     */
    fun parseBulkText(text: String): List<ParsedChapter> {
        val lines = text.lines()
        val result = mutableListOf<ParsedChapter>()
        var chapterIndex = 1

        for (line in lines) {
            val parsed = parseLine(line, defaultChapterNumber = chapterIndex)
            if (parsed != null) {
                result.add(parsed)
                chapterIndex++
            }
        }
        return result
    }

    /**
     * Formats a list of chapters into the "title|pageCount" string format for editing.
     */
    fun formatChapters(chapters: List<Chapter>): String {
        return chapters.joinToString("\n") { chapter ->
            val title = chapter.customTitle.ifBlank { "Chapter ${chapter.number}" }
            if (chapter.pageCount > 0) {
                "$title|${chapter.pageCount}"
            } else {
                title
            }
        }
    }
}
