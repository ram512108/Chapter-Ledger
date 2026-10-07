package com.example.domain

import com.example.data.repository.BookRepository
import com.example.model.Book
import com.example.model.BookFormat
import com.example.model.BookStatus
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object ExportImportManager {

    fun exportToJson(books: List<Book>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Chapterly")
        root.put("exportDate", System.currentTimeMillis())

        val booksArray = JSONArray()
        books.forEach { book ->
            val obj = JSONObject().apply {
                put("id", book.id)
                put("title", book.title)
                put("author", book.author)
                put("isbn", book.isbn)
                put("coverImageUrl", book.coverImageUrl ?: "")
                put("totalChapters", book.totalChapters)
                put("totalPages", book.totalPages)
                put("genres", JSONArray(book.genres))
                put("status", book.status.name)
                put("format", book.format.name)
                put("rating", book.rating.toDouble())
                put("dateAdded", book.dateAdded)
                put("isFavorite", book.isFavorite)
                put("seriesName", book.seriesName ?: "")
                put("seriesPosition", (book.seriesPosition ?: 0f).toDouble())
            }
            booksArray.put(obj)
        }
        root.put("books", booksArray)
        return root.toString(2)
    }

    suspend fun importFromJson(jsonString: String, bookRepository: BookRepository): Int {
        val root = JSONObject(jsonString)
        val booksArray = root.getJSONArray("books")
        var count = 0

        for (i in 0 until booksArray.length()) {
            val obj = booksArray.getJSONObject(i)
            val genresList = mutableListOf<String>()
            val genresArr = obj.optJSONArray("genres")
            if (genresArr != null) {
                for (j in 0 until genresArr.length()) {
                    genresList.add(genresArr.getString(j))
                }
            }

            val book = Book(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.getString("title"),
                author = obj.getString("author"),
                isbn = obj.optString("isbn", ""),
                coverImageUrl = obj.optString("coverImageUrl").takeIf { it.isNotBlank() },
                totalChapters = obj.optInt("totalChapters", 10),
                totalPages = obj.optInt("totalPages", 0),
                genres = genresList,
                status = BookStatus.fromString(obj.optString("status", "WANT_TO_READ")),
                format = BookFormat.fromString(obj.optString("format", "PHYSICAL")),
                rating = obj.optDouble("rating", 0.0).toFloat(),
                dateAdded = obj.optLong("dateAdded", System.currentTimeMillis()),
                isFavorite = obj.optBoolean("isFavorite", false),
                seriesName = obj.optString("seriesName").takeIf { it.isNotBlank() },
                seriesPosition = obj.optDouble("seriesPosition", 0.0).toFloat().takeIf { it > 0 }
            )

            bookRepository.insertBookWithChapters(book)
            count++
        }
        return count
    }

    suspend fun importGoodreadsCsv(csvContent: String, bookRepository: BookRepository): Int {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return 0

        val header = parseCsvLine(lines[0])
        val titleIdx = header.indexOfFirst { it.equals("Title", ignoreCase = true) }
        val authorIdx = header.indexOfFirst { it.equals("Author", ignoreCase = true) }
        val isbnIdx = header.indexOfFirst { it.equals("ISBN13", ignoreCase = true) }.takeIf { it >= 0 }
            ?: header.indexOfFirst { it.equals("ISBN", ignoreCase = true) }
        val pagesIdx = header.indexOfFirst { it.equals("Number of Pages", ignoreCase = true) }
        val shelfIdx = header.indexOfFirst { it.equals("Exclusive Shelf", ignoreCase = true) }
        val ratingIdx = header.indexOfFirst { it.equals("My Rating", ignoreCase = true) }

        if (titleIdx < 0 || authorIdx < 0) return 0

        var importedCount = 0
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size <= maxOf(titleIdx, authorIdx)) continue

            val title = cols[titleIdx].trim()
            val author = cols[authorIdx].trim()
            if (title.isBlank()) continue

            val isbn = if (isbnIdx in cols.indices) cols[isbnIdx].replace("=\"", "").replace("\"", "").trim() else ""
            val pages = if (pagesIdx in cols.indices) cols[pagesIdx].toIntOrNull() ?: 0 else 0
            val shelf = if (shelfIdx in cols.indices) cols[shelfIdx].lowercase().trim() else "to-read"
            val rating = if (ratingIdx in cols.indices) cols[ratingIdx].toFloatOrNull() ?: 0f else 0f

            val status = when (shelf) {
                "currently-reading" -> BookStatus.READING
                "read" -> BookStatus.FINISHED
                else -> BookStatus.WANT_TO_READ
            }

            // Estimate chapter count from pages if chapters not known (1 chapter ~ 20 pages)
            val estimatedChapters = if (pages > 0) (pages / 20).coerceIn(5, 50) else 12

            val book = Book(
                id = UUID.randomUUID().toString(),
                title = title,
                author = author,
                isbn = isbn,
                totalChapters = estimatedChapters,
                totalPages = pages,
                status = status,
                rating = rating,
                genres = listOf("Imported")
            )

            bookRepository.insertBookWithChapters(book)
            importedCount++
        }
        return importedCount
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var inQuotes = false
        val sb = StringBuilder()

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}
