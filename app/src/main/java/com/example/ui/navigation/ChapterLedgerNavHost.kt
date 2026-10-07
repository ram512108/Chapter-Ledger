package com.example.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ChapterlyApp
import com.example.ui.components.ChapterlyBottomBar
import com.example.ui.screens.AddBookScreen
import com.example.ui.screens.BookDetailScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.QuotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.viewmodel.AddBookViewModel
import com.example.ui.viewmodel.BookDetailViewModel
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.QuotesViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.StatsViewModel

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun ChapterlyNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val app = LocalContext.current.applicationContext as ChapterlyApp
    val container = app.container

    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.Library.route,
            title = "Library",
            selectedIcon = Icons.Filled.AutoStories,
            unselectedIcon = Icons.Outlined.AutoStories,
            testTag = "nav_library"
        ),
        BottomNavItem(
            route = Screen.Stats.route,
            title = "Stats",
            selectedIcon = Icons.Filled.Insights,
            unselectedIcon = Icons.Outlined.Insights,
            testTag = "nav_stats"
        ),
        BottomNavItem(
            route = Screen.Quotes.route,
            title = "Quotes",
            selectedIcon = Icons.Filled.FormatQuote,
            unselectedIcon = Icons.Outlined.FormatQuote,
            testTag = "nav_quotes"
        ),
        BottomNavItem(
            route = Screen.Settings.route,
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            testTag = "nav_settings"
        )
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                ChapterlyBottomBar(
                    currentRoute = currentRoute,
                    items = bottomNavItems,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Library.route) {
                val libraryViewModel: LibraryViewModel = viewModel(
                    factory = LibraryViewModel.Factory(
                        bookRepository = container.bookRepository,
                        preferencesRepository = container.preferencesRepository,
                        sessionRepository = container.readingSessionRepository
                    )
                )
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onBookClick = { bookId ->
                        navController.navigate(Screen.BookDetail.createRoute(bookId))
                    },
                    onAddBookClick = {
                        navController.navigate(Screen.AddBook.createRoute())
                    }
                )
            }

            composable(
                route = Screen.BookDetail.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                val bookDetailViewModel: BookDetailViewModel = viewModel(
                    factory = BookDetailViewModel.Factory(
                        bookId = bookId,
                        bookRepository = container.bookRepository,
                        sessionRepository = container.readingSessionRepository,
                        preferencesRepository = container.preferencesRepository
                    )
                )
                BookDetailScreen(
                    viewModel = bookDetailViewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditBook = { bId -> navController.navigate(Screen.AddBook.createRoute(bId)) }
                )
            }

            composable(
                route = Screen.AddBook.route,
                arguments = listOf(navArgument("editBookId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val rawEditBookId = backStackEntry.arguments?.getString("editBookId")
                val editBookId = rawEditBookId?.takeIf {
                    it.isNotBlank() && it != "{editBookId}" && it != "null" && it != "undefined" && it != "new"
                }
                val addBookViewModel: AddBookViewModel = viewModel(
                    key = editBookId ?: "add_book_new_${backStackEntry.id}",
                    factory = AddBookViewModel.Factory(
                        bookRepository = container.bookRepository,
                        openLibraryService = container.openLibraryService,
                        editBookId = editBookId
                    )
                )
                AddBookScreen(
                    viewModel = addBookViewModel,
                    onBackClick = { navController.popBackStack() },
                    onBookSaved = { navController.popBackStack() }
                )
            }

            composable(Screen.Stats.route) {
                val statsViewModel: StatsViewModel = viewModel(
                    factory = StatsViewModel.Factory(
                        bookRepository = container.bookRepository,
                        sessionRepository = container.readingSessionRepository,
                        goalRepository = container.goalRepository,
                        preferencesRepository = container.preferencesRepository,
                        database = container.database
                    )
                )
                StatsScreen(
                    viewModel = statsViewModel,
                    onBookClick = { bookId ->
                        navController.navigate(Screen.BookDetail.createRoute(bookId))
                    }
                )
            }

            composable(Screen.Quotes.route) {
                val quotesViewModel: QuotesViewModel = viewModel(
                    factory = QuotesViewModel.Factory(
                        bookRepository = container.bookRepository
                    )
                )
                QuotesScreen(
                    viewModel = quotesViewModel,
                    onBookClick = { bookId ->
                        navController.navigate(Screen.BookDetail.createRoute(bookId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        bookRepository = container.bookRepository,
                        sessionRepository = container.readingSessionRepository,
                        preferencesRepository = container.preferencesRepository,
                        database = container.database
                    )
                )
                SettingsScreen(
                    viewModel = settingsViewModel
                )
            }
        }
    }
}
