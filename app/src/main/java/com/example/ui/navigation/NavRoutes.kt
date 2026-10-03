package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: String) = "book_detail/$bookId"
    }
    object AddBook : Screen("add_book?editBookId={editBookId}") {
        fun createRoute(editBookId: String? = null): String {
            return if (!editBookId.isNullOrBlank() && editBookId != "{editBookId}" && editBookId != "null" && editBookId != "undefined" && editBookId != "new") {
                "add_book?editBookId=$editBookId"
            } else {
                "add_book"
            }
        }
    }
    object Stats : Screen("stats")
    object Quotes : Screen("quotes")
    object Settings : Screen("settings")
}
