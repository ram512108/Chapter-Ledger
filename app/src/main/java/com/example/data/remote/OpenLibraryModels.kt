package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenLibrarySearchResponse(
    @Json(name = "numFound") val numFound: Int = 0,
    @Json(name = "docs") val docs: List<OpenLibraryDoc> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OpenLibraryDoc(
    @Json(name = "key") val key: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "author_name") val authorName: List<String>? = null,
    @Json(name = "isbn") val isbn: List<String>? = null,
    @Json(name = "cover_i") val coverI: Long? = null,
    @Json(name = "number_of_pages_median") val numberOfPagesMedian: Int? = null,
    @Json(name = "first_publish_year") val firstPublishYear: Int? = null,
    @Json(name = "subject") val subject: List<String>? = null
) {
    val coverUrl: String?
        get() = coverI?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }

    val author: String
        get() = authorName?.firstOrNull() ?: "Unknown Author"

    val firstIsbn: String
        get() = isbn?.firstOrNull() ?: ""
}

data class BookSearchResult(
    val title: String,
    val author: String,
    val isbn: String,
    val coverImageUrl: String?,
    val estimatedPages: Int,
    val subjects: List<String>
)
