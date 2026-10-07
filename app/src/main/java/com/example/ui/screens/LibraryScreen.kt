package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ActiveReadingSession
import com.example.model.Book
import com.example.model.BookFormat
import com.example.model.BookStatus
import com.example.ui.components.BookCoverImage
import com.example.ui.components.FuturisticFilterChip
import com.example.ui.components.FuturisticPrimaryButton
import com.example.ui.components.FuturisticSecondaryButton
import com.example.ui.components.FuturisticProgressBar
import com.example.ui.components.GlassCard
import com.example.ui.components.ReadingSessionSheet
import kotlinx.coroutines.delay
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BorderMedium
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChapterPinnedGold
import com.example.ui.theme.ChapterReadGreen
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHigherElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.LibraryUiState
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBookClick: (String) -> Unit,
    onAddBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isSortMenuOpen by remember { mutableStateOf(false) }
    var isSessionSheetOpen by rememberSaveable { mutableStateOf(false) }
    var hasAutoOpenedSession by rememberSaveable { mutableStateOf(false) }

    // Auto-restore active session popup when app/Home is reopened
    LaunchedEffect(state.activeSession?.id) {
        if (state.activeSession != null && !hasAutoOpenedSession) {
            isSessionSheetOpen = true
            hasAutoOpenedSession = true
        } else if (state.activeSession == null) {
            hasAutoOpenedSession = false
            isSessionSheetOpen = false
        }
    }

    Scaffold(
        modifier = modifier.testTag("library_screen"),
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = {
                                Text(
                                    "Search title, author, genre...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextTertiary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                                .testTag("library_search_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceElevated,
                                unfocusedContainerColor = SurfaceCard,
                                focusedBorderColor = LavenderAccent,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.onSearchQueryChange("")
                                    isSearchExpanded = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        )
                    } else {
                        Column {
                            Text(
                                text = "Chapterly",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.books.size} Books · ${state.currentlyReadingCount} Reading · ${state.completedCount} Read",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(
                            onClick = { isSearchExpanded = true },
                            modifier = Modifier.testTag("search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextPrimary
                            )
                        }
                    }

                    // Grid / List toggle
                    IconButton(
                        onClick = viewModel::toggleGridMode,
                        modifier = Modifier.testTag("view_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (state.isGridMode) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle Grid/List View",
                            tint = TextPrimary
                        )
                    }

                    // Sort menu
                    Box {
                        IconButton(
                            onClick = { isSortMenuOpen = true },
                            modifier = Modifier.testTag("sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort",
                                tint = TextPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = isSortMenuOpen,
                            onDismissRequest = { isSortMenuOpen = false },
                            modifier = Modifier
                                .background(SurfaceElevated)
                                .border(BorderStroke(1.dp, BorderMedium), RoundedCornerShape(8.dp))
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (state.sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                                color = if (state.sortOption == option) LavenderAccent else TextPrimary
                                            )
                                        )
                                    },
                                    onClick = {
                                        viewModel.onSortSelected(option)
                                        isSortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBookClick,
                containerColor = LavenderAccent,
                contentColor = Color(0xFF07090B),
                shape = RoundedCornerShape(16.dp),
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.testTag("add_book_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Book",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Horizontal Filter Chips
            FilterChipsRow(state = state, viewModel = viewModel)

            // Active Reading Session Banner
            if (state.activeSession != null) {
                ActiveReadingSessionBanner(
                    session = state.activeSession!!,
                    onResumeClick = { isSessionSheetOpen = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // Reading Progress Summary Card
            if (state.books.isNotEmpty()) {
                ReadingOverviewBanner(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // Content: Grid or List
            if (state.filteredBooks.isEmpty()) {
                EmptyLibraryState(
                    hasQuery = state.searchQuery.isNotBlank() || state.selectedStatus != null || state.favoritesOnly || state.selectedFormat != null || state.selectedShelfId != null,
                    onAddClick = onAddBookClick,
                    onClearFilters = viewModel::clearAllFilters
                )
            } else {
                if (state.isGridMode) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.filteredBooks, key = { it.id }) { book ->
                            BookGridCard(
                                book = book,
                                onClick = { onBookClick(book.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(book.id, !book.isFavorite) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.filteredBooks, key = { it.id }) { book ->
                            BookListRow(
                                book = book,
                                onClick = { onBookClick(book.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(book.id, !book.isFavorite) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (isSessionSheetOpen && state.activeSession != null) {
        val activeSess = state.activeSession!!
        val sessionBook = state.books.find { it.id == activeSess.bookId } ?: Book(
            id = activeSess.bookId,
            title = activeSess.bookTitle.ifBlank { "Book" },
            author = ""
        )

        ReadingSessionSheet(
            book = sessionBook,
            activeSession = activeSess,
            onDismiss = { isSessionSheetOpen = false },
            onToggleTimer = viewModel::toggleActiveSessionTimer,
            onResetSession = {
                isSessionSheetOpen = false
                viewModel.resetActiveSession()
            },
            onUpdateDraft = viewModel::updateActiveSessionDraft,
            onFinishSession = { session ->
                isSessionSheetOpen = false
                viewModel.finishActiveSession(session)
            }
        )
    }
}

@Composable
private fun FilterChipsRow(
    state: LibraryUiState,
    viewModel: LibraryViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favorites Chip
        FuturisticFilterChip(
            selected = state.favoritesOnly,
            onClick = viewModel::onToggleFavoritesOnly,
            label = "Favorites",
            leadingIcon = if (state.favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            activeColor = Color(0xFFF43F5E)
        )

        // Status Chips
        BookStatus.entries.forEach { status ->
            FuturisticFilterChip(
                selected = state.selectedStatus == status,
                onClick = { viewModel.onStatusSelected(status) },
                label = status.displayName,
                activeColor = when (status) {
                    BookStatus.READING -> AmberPrimary
                    BookStatus.FINISHED -> ChapterReadGreen
                    BookStatus.WANT_TO_READ -> LavenderAccent
                    else -> LavenderAccent
                }
            )
        }

        // Format Chips
        BookFormat.entries.forEach { format ->
            FuturisticFilterChip(
                selected = state.selectedFormat == format,
                onClick = { viewModel.onFormatSelected(format) },
                label = format.displayName,
                activeColor = LavenderAccent
            )
        }

        // Shelves
        state.shelves.forEach { shelf ->
            FuturisticFilterChip(
                selected = state.selectedShelfId == shelf.id,
                onClick = { viewModel.onShelfSelected(shelf.id) },
                label = shelf.name,
                activeColor = LavenderAccent
            )
        }
    }
}

@Composable
fun BookGridCard(
    book: Book,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_card_${book.id}"),
        backgroundColor = SurfaceCard,
        borderColor = BorderSubtle,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Book Cover Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
            ) {
                BookCoverImage(
                    coverUrl = book.coverImageUrl,
                    title = book.title,
                    author = book.author,
                    modifier = Modifier.fillMaxSize()
                )

                // Favorite button (top-right)
                Surface(
                    shape = CircleShape,
                    color = Color(0xCC07090B),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(30.dp)
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("favorite_button_${book.id}")
                    ) {
                        Icon(
                            imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (book.isFavorite) Color(0xFFF43F5E) else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Format badge (bottom-left)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xDD0B0F12),
                    border = BorderStroke(0.5.dp, BorderSubtle),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = book.format.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Pinned tag (bottom-right if pinned)
                if (book.pinnedNextChapterId != null) {
                    Surface(
                        shape = CircleShape,
                        color = AmberGlow,
                        border = BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Pinned Next",
                                tint = AmberPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title (2-line clamp max)
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp
                ),
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Author (1-line muted)
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            val progressFraction = when {
                book.totalPages > 0 && book.currentPage > 0 -> (book.currentPage.toFloat() / book.totalPages).coerceIn(0f, 1f)
                book.totalChapters > 0 -> (book.completedChaptersCount.toFloat() / book.totalChapters).coerceIn(0f, 1f)
                else -> 0f
            }

            FuturisticProgressBar(
                progress = progressFraction,
                height = 5.dp,
                trackColor = SurfaceHigherElevated
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Progress text row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (book.totalPages > 0) {
                        "p. ${book.currentPage}/${book.totalPages}"
                    } else {
                        "${book.completedChaptersCount}/${book.totalChapters} ch"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextTertiary
                )
                Text(
                    text = "${book.progressPercentage}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (book.status == BookStatus.FINISHED) ChapterReadGreen else AmberPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun BookListRow(
    book: Book,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_row_${book.id}"),
        backgroundColor = SurfaceCard,
        borderColor = BorderSubtle,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(78.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated)
            ) {
                BookCoverImage(
                    coverUrl = book.coverImageUrl,
                    title = book.title,
                    author = book.author,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("favorite_button_${book.id}")
                    ) {
                        Icon(
                            imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (book.isFavorite) Color(0xFFF43F5E) else TextTertiary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Text(
                    text = "${book.author} · ${book.format.displayName}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress
                val progressFraction = when {
                    book.totalPages > 0 && book.currentPage > 0 -> (book.currentPage.toFloat() / book.totalPages).coerceIn(0f, 1f)
                    book.totalChapters > 0 -> (book.completedChaptersCount.toFloat() / book.totalChapters).coerceIn(0f, 1f)
                    else -> 0f
                }

                FuturisticProgressBar(
                    progress = progressFraction,
                    height = 5.dp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (book.totalPages > 0) {
                            "p. ${book.currentPage}/${book.totalPages} · ${book.status.displayName}"
                        } else {
                            "${book.completedChaptersCount}/${book.totalChapters} ch · ${book.status.displayName}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextTertiary
                    )
                    Text(
                        text = "${book.progressPercentage}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (book.status == BookStatus.FINISHED) ChapterReadGreen else AmberPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadingOverviewBanner(
    state: LibraryUiState,
    modifier: Modifier = Modifier
) {
    val totalChapters = state.books.sumOf { it.totalChapters }
    val completedChapters = state.books.sumOf { it.completedChaptersCount }
    val overallPercent = if (totalChapters > 0) {
        ((completedChapters.toFloat() / totalChapters) * 100).toInt()
    } else 0

    GlassCard(
        modifier = modifier.testTag("reading_overview_card"),
        backgroundColor = SurfaceCard,
        borderColor = BorderSubtle,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AmberGlow,
                        border = BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Reading Journey",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = (-0.1).sp
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "$completedChapters of $totalChapters chapters completed",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, BorderMedium)
                ) {
                    Text(
                        text = "$overallPercent%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AmberPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progressFraction = if (totalChapters > 0) completedChapters.toFloat() / totalChapters else 0f
            FuturisticProgressBar(
                progress = progressFraction,
                height = 7.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${state.books.size} books on ledger",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextTertiary
                )
                Text(
                    text = "${state.currentlyReadingCount} active progress · ${state.completedCount} finished",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(
    hasQuery: Boolean,
    onAddClick: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("empty_library_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasQuery) {
            GlassCard(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = SurfaceCard,
                borderColor = BorderSubtle,
                modifier = Modifier.size(180.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_empty_library_1789663022815),
                        contentDescription = "Empty Library Illustration",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your Reading Ledger is Empty",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                textAlign = TextAlign.Center,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add your first book to track chapters in any order, mark milestones, and keep notes on your journey.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                textAlign = TextAlign.Center,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            FuturisticPrimaryButton(
                text = "Add Your First Book",
                onClick = onAddClick,
                icon = Icons.Default.Add,
                modifier = Modifier.testTag("empty_state_add_book_button")
            )
        } else {
            Surface(
                shape = CircleShape,
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = LavenderAccent,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "No Matching Books Found",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                textAlign = TextAlign.Center,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Try adjusting your search keywords, shelves, or filter chips.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                textAlign = TextAlign.Center,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FuturisticSecondaryButton(
                text = "Clear All Filters",
                onClick = onClearFilters,
                modifier = Modifier.testTag("clear_filters_button")
            )
        }
    }
}

@Composable
fun ActiveReadingSessionBanner(
    session: ActiveReadingSession,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTimeMillis by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = session.isRunning,
        key2 = session.lastResumeTime
    ) {
        while (session.isRunning) {
            delay(500L)
            value = System.currentTimeMillis()
        }
    }

    val formattedTime = session.getFormattedTime(currentTimeMillis)

    GlassCard(
        backgroundColor = SurfaceElevated,
        borderColor = AmberPrimary.copy(alpha = 0.6f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onResumeClick() }
            .testTag("active_reading_session_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AmberGlow,
                    border = BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.6f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ACTIVE SESSION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            ),
                            color = AmberPrimary
                        )
                        if (!session.isRunning) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• PAUSED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = TextTertiary
                            )
                        }
                    }
                    Text(
                        text = session.bookTitle.ifBlank { "Reading Book" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AmberPrimary,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFF07090B)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF07090B),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
