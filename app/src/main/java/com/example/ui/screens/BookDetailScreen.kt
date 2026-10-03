package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.model.Book
import com.example.model.BookStatus
import com.example.model.Chapter
import com.example.model.ReadingSession
import com.example.ui.components.BookCoverImage
import com.example.ui.components.BulkChaptersDialog
import com.example.ui.components.ChapterDetailDialog
import com.example.ui.components.ChapterGridItem
import com.example.ui.components.ChapterOption
import com.example.ui.components.ChapterTimelineView
import com.example.ui.components.FuturisticPrimaryButton
import com.example.ui.components.FuturisticProgressBar
import com.example.ui.components.FuturisticSecondaryButton
import com.example.ui.components.GlassCard
import com.example.ui.components.QuoteEditDialog
import com.example.ui.components.ReadingSessionSheet
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BorderMedium
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChapterPinnedGold
import com.example.ui.theme.ChapterReadGreen
import com.example.ui.theme.ChapterReadGreenGlow
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHigherElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.BookDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    viewModel: BookDetailViewModel,
    onBackClick: () -> Unit,
    onEditBook: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val book = state.book ?: return
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPageTrackerDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var quoteToEdit by remember { mutableStateOf<Triple<Chapter, Int, String>?>(null) }
    val tabTitles = listOf("Chapters", "Timeline", "Notes & Quotes", "Sessions")

    if (showExportDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Book JSON Export", color = TextPrimary) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    Text("Copy or view book details in JSON format:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Book JSON", exportedJsonText))
                        Toast.makeText(context, "Copied JSON to clipboard", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent)
                ) {
                    Text("Copy to Clipboard", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = SurfaceElevated
        )
    }

    Scaffold(
        modifier = modifier.testTag("book_detail_screen"),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = book.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val json = com.example.domain.ExportImportManager.exportToJson(listOf(book))
                            exportedJsonText = json
                            showExportDialog = true
                        },
                        modifier = Modifier.testTag("export_json_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Export JSON",
                            tint = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { onEditBook(book.id) },
                        modifier = Modifier.testTag("edit_book_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Book",
                            tint = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = viewModel::toggleFavorite,
                        modifier = Modifier.testTag("favorite_button")
                    ) {
                        Icon(
                            imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (book.isFavorite) Color(0xFFF43F5E) else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("delete_book_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove Book",
                            tint = Color(0xFFF43F5E).copy(alpha = 0.9f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Book Header summary
            BookDetailHeader(
                book = book,
                onStatusChange = viewModel::updateBookStatus,
                onRatingChange = viewModel::updateBookRating
            )

            // Page Reading Tracker Card
            PageTrackingCard(
                book = book,
                onOpenPageDialog = { showPageTrackerDialog = true },
                onIncrementPage = { step ->
                    viewModel.incrementCurrentPage(step)
                    Toast.makeText(context, "Advanced +$step pages", Toast.LENGTH_SHORT).show()
                }
            )

            // Pinned Chapter Hero Card (if exists)
            val pinnedChapter = state.chapters.firstOrNull { it.isPinnedNext }
            if (pinnedChapter != null) {
                PinnedChapterBanner(
                    chapter = pinnedChapter,
                    onMarkRead = { viewModel.toggleChapter(pinnedChapter.id) },
                    onOpenDetails = { viewModel.selectChapterForDetails(pinnedChapter) }
                )
            }

            // Quick actions (Bulk edit titles, Log session)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FuturisticSecondaryButton(
                    text = "Bulk Chapters & Pages",
                    onClick = { viewModel.setBulkEditorVisible(true) },
                    icon = Icons.Default.Edit,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bulk_edit_chapters_button")
                )

                val currentActiveSession = state.activeSession
                val isSessionActiveForThisBook = currentActiveSession?.bookId == book.id
                val sessionButtonText = if (isSessionActiveForThisBook) "Resume Session" else "Start Session"
                val sessionButtonIcon = if (isSessionActiveForThisBook) Icons.Default.Timer else Icons.Default.PlayArrow

                FuturisticPrimaryButton(
                    text = sessionButtonText,
                    onClick = { viewModel.setReadingSessionSheetVisible(true) },
                    icon = sessionButtonIcon,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("start_reading_session_button")
                )
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 14.dp,
                containerColor = BackgroundDark,
                contentColor = LavenderAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = LavenderAccent,
                        height = 2.5.dp
                    )
                },
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderSubtle)
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.5.sp
                                ),
                                color = if (selectedTabIndex == index) LavenderAccent else TextSecondary
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> ChaptersMatrixTab(
                        chapters = state.chapters,
                        onToggleRead = viewModel::toggleChapter,
                        onOpenDetails = viewModel::selectChapterForDetails
                    )
                    1 -> ChapterTimelineView(
                        chapters = state.chapters,
                        onToggleRead = viewModel::toggleChapter,
                        onChapterClick = viewModel::selectChapterForDetails
                    )
                    2 -> NotesAndQuotesTab(
                        chapters = state.chapters,
                        onEditQuote = { ch, idx, text -> quoteToEdit = Triple(ch, idx, text) }
                    )
                    3 -> SessionsTab(sessions = state.sessions)
                }
            }
        }
    }

    // Dialogs
    state.selectedChapterForDetails?.let { chapter ->
        ChapterDetailDialog(
            chapter = chapter,
            onDismiss = { viewModel.selectChapterForDetails(null) },
            onSave = { title, notes, quotes, rating, pageCount ->
                viewModel.saveChapterDetails(chapter.id, title, notes, quotes, rating, pageCount)
            },
            onToggleRead = { viewModel.toggleChapter(chapter.id) },
            onTogglePin = {
                if (chapter.isPinnedNext) viewModel.unpinChapter(chapter.id)
                else viewModel.pinChapter(chapter.id)
            }
        )
    }

    if (state.showBulkEditor) {
        val currentFormattedChapters = com.example.domain.ChapterParser.formatChapters(state.chapters)
        BulkChaptersDialog(
            initialText = currentFormattedChapters,
            onDismiss = { viewModel.setBulkEditorVisible(false) },
            onSave = viewModel::saveBulkChapters
        )
    }

    val activeSess = state.activeSession
    if (state.showReadingSessionSheet && activeSess != null) {
        ReadingSessionSheet(
            book = book,
            activeSession = activeSess,
            onDismiss = { viewModel.setReadingSessionSheetVisible(false) },
            onToggleTimer = viewModel::toggleSessionTimer,
            onResetSession = viewModel::resetActiveSession,
            onUpdateDraft = viewModel::updateSessionDraft,
            onFinishSession = viewModel::finishReadingSession
        )
    }

    quoteToEdit?.let { (chapter, quoteIndex, quoteText) ->
        val chapterOptions = state.chapters.map {
            ChapterOption(id = it.id, number = it.number, title = it.displayTitle)
        }
        QuoteEditDialog(
            initialQuote = quoteText,
            initialNotes = chapter.notes,
            initialPageNumber = chapter.pageCount,
            initialIsFavorite = book.isFavorite || chapter.rating >= 4f,
            bookTitle = book.title,
            author = book.author,
            currentChapterId = chapter.id,
            availableChapters = chapterOptions,
            onDismiss = { quoteToEdit = null },
            onSave = { updatedQuote, notes, page, targetChId, isFav ->
                viewModel.updateSavedQuote(
                    chapterId = chapter.id,
                    quoteIndex = quoteIndex,
                    newQuoteText = updatedQuote,
                    newNotes = notes,
                    newPageNumber = page,
                    targetChapterId = targetChId,
                    isFavorite = isFav
                )
                quoteToEdit = null
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp),
            containerColor = BackgroundSecondary,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Remove Book from Ledger?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${book.title}\"? This will permanently delete the book, all chapter records, notes, quotes, and reading sessions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteBook {
                            Toast.makeText(context, "\"${book.title}\" removed from library", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE11D48),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_book_button")
                ) {
                    Text("Delete Book", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextTertiary)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPageTrackerDialog) {
        var pageInput by remember { mutableStateOf(book.currentPage.toString()) }
        var totalInput by remember { mutableStateOf(if (book.totalPages > 0) book.totalPages.toString() else "") }
        AlertDialog(
            onDismissRequest = { showPageTrackerDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp),
            containerColor = BackgroundSecondary,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Update Reading Page",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Set your current page marker and book total pages.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                    OutlinedTextField(
                        value = pageInput,
                        onValueChange = { pageInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Current Page", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_current_page_input")
                    )
                    OutlinedTextField(
                        value = totalInput,
                        onValueChange = { totalInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Total Pages", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = LavenderAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_total_pages_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val page = pageInput.toIntOrNull() ?: 0
                        val total = totalInput.toIntOrNull() ?: 0
                        viewModel.updateCurrentPage(page)
                        if (total > 0) {
                            viewModel.updateTotalPages(total)
                        }
                        showPageTrackerDialog = false
                        Toast.makeText(context, "Page updated to $page", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberPrimary,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_page_update_button")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPageTrackerDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextTertiary)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PageTrackingCard(
    book: Book,
    onOpenPageDialog: () -> Unit,
    onIncrementPage: (Int) -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("page_tracking_card"),
        backgroundColor = SurfaceCard,
        borderColor = BorderSubtle,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = LavenderAccent,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "PAGE TRACKER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.5.sp
                        ),
                        color = LavenderAccent
                    )
                }

                Surface(
                    onClick = onOpenPageDialog,
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, BorderMedium)
                ) {
                    Text(
                        text = "Set Page",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = AmberPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (book.totalPages > 0) {
                        "Page ${book.currentPage} of ${book.totalPages}"
                    } else {
                        "Page ${book.currentPage} (total not set)"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = TextPrimary
                )

                Text(
                    text = if (book.totalPages > 0) "${book.pageProgressPercentage}%" else "",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (book.status == BookStatus.FINISHED) ChapterReadGreen else AmberPrimary
                    )
                )
            }

            if (book.totalPages > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                val pageFrac = (book.currentPage.toFloat() / book.totalPages).coerceIn(0f, 1f)
                FuturisticProgressBar(
                    progress = pageFrac,
                    height = 5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(LavenderAccent, Color(0xFFA78BFA))
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick increment buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick advance:",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = TextTertiary,
                    modifier = Modifier.padding(end = 2.dp)
                )

                listOf(1, 5, 10, 25).forEach { step ->
                    Surface(
                        onClick = { onIncrementPage(step) },
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceElevated,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.testTag("quick_page_plus_$step")
                    ) {
                        Text(
                            text = "+$step",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookDetailHeader(
    book: Book,
    onStatusChange: (BookStatus) -> Unit,
    onRatingChange: (Float) -> Unit
) {
    var isStatusMenuOpen by remember { mutableStateOf(false) }

    GlassCard(
        backgroundColor = SurfaceCard,
        borderColor = BorderSubtle,
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(76.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
            ) {
                BookCoverImage(
                    coverUrl = book.coverImageUrl,
                    title = book.title,
                    author = book.author,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.1).sp
                    ),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status Dropdown & Format
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box {
                        Surface(
                            onClick = { isStatusMenuOpen = true },
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceElevated,
                            border = BorderStroke(1.dp, BorderMedium)
                        ) {
                            Text(
                                text = book.status.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = when (book.status) {
                                    BookStatus.READING -> AmberPrimary
                                    BookStatus.FINISHED -> ChapterReadGreen
                                    else -> LavenderAccent
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isStatusMenuOpen,
                            onDismissRequest = { isStatusMenuOpen = false },
                            modifier = Modifier
                                .background(SurfaceElevated)
                                .border(BorderStroke(1.dp, BorderMedium), RoundedCornerShape(8.dp))
                        ) {
                            BookStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = status.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (book.status == status) LavenderAccent else TextPrimary
                                            )
                                        )
                                    },
                                    onClick = {
                                        onStatusChange(status)
                                        isStatusMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceElevated,
                        border = BorderStroke(0.5.dp, BorderSubtle)
                    ) {
                        Text(
                            text = book.format.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextTertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Indicator
                val progressFraction = when {
                    book.totalPages > 0 && book.currentPage > 0 -> (book.currentPage.toFloat() / book.totalPages).coerceIn(0f, 1f)
                    book.totalChapters > 0 -> (book.completedChaptersCount.toFloat() / book.totalChapters).coerceIn(0f, 1f)
                    else -> 0f
                }

                FuturisticProgressBar(
                    progress = progressFraction,
                    height = 6.dp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (book.totalPages > 0) {
                            "p. ${book.currentPage}/${book.totalPages} · ${book.completedChaptersCount}/${book.totalChapters} ch"
                        } else {
                            "${book.completedChaptersCount} of ${book.totalChapters} chapters read"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
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
}

@Composable
private fun PinnedChapterBanner(
    chapter: Chapter,
    onMarkRead: () -> Unit,
    onOpenDetails: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clickable(onClick = onOpenDetails)
            .testTag("pinned_chapter_banner"),
        backgroundColor = Color(0xFF191610),
        borderColor = AmberPrimary.copy(alpha = 0.7f),
        borderWidth = 1.5.dp,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AmberGlow,
                    border = BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "READ NEXT · CHAPTER ${chapter.number}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            color = AmberPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = chapter.displayTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onMarkRead,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberPrimary,
                    contentColor = Color(0xFF07090B)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("mark_pinned_read_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Mark Read",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ChaptersMatrixTab(
    chapters: List<Chapter>,
    onToggleRead: (String) -> Unit,
    onOpenDetails: (Chapter) -> Unit
) {
    if (chapters.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No chapters configured.", color = TextTertiary)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chapters, key = { it.id }) { chapter ->
                ChapterGridItem(
                    chapter = chapter,
                    onToggleRead = { onToggleRead(chapter.id) },
                    onOpenDetails = { onOpenDetails(chapter) }
                )
            }
        }
    }
}

@Composable
private fun ReadingOrderTimelineTab(
    timeline: List<Chapter>,
    onOpenDetails: (Chapter) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    if (timeline.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = LavenderAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No reading sequence recorded yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "As you mark chapters read in any order, your custom non-linear reading timeline appears here!",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Non-Linear Reading Sequence (${timeline.size} Completed)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LavenderAccent
                    )
                )
            }

            items(timeline, key = { it.id }) { chapter ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenDetails(chapter) },
                    backgroundColor = SurfaceCard,
                    borderColor = BorderSubtle,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ChapterReadGreenGlow,
                            border = BorderStroke(1.dp, ChapterReadGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#${chapter.readingOrderIndex ?: 1}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ChapterReadGreen
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chapter ${chapter.number}: ${chapter.displayTitle}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            chapter.dateRead?.let { date ->
                                Text(
                                    text = "Read on ${dateFormat.format(Date(date))}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                                )
                            }
                            if (chapter.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = chapter.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesAndQuotesTab(
    chapters: List<Chapter>,
    onEditQuote: (chapter: Chapter, quoteIndex: Int, quoteText: String) -> Unit
) {
    val withNotes = chapters.filter { it.notes.isNotBlank() || it.highlightedQuotes.isNotEmpty() }

    if (withNotes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No notes or quotes recorded yet. Tap any chapter to add highlights and reflections.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(withNotes, key = { it.id }) { ch ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceCard,
                    borderColor = BorderSubtle,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Chapter ${ch.number}: ${ch.displayTitle}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberPrimary
                            )
                        )

                        if (ch.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ch.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }

                        if (ch.highlightedQuotes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ch.highlightedQuotes.forEachIndexed { idx, quote ->
                                Card(
                                    onClick = { onEditQuote(ch, idx, quote) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .testTag("book_detail_quote_item")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FormatQuote,
                                            contentDescription = null,
                                            tint = LavenderAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "“$quote”",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = TextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionsTab(sessions: List<ReadingSession>) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    if (sessions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No reading sessions logged yet for this book. Tap 'Start Session' above to track your reading time.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        val totalMinutes = sessions.sumOf { it.durationMinutes }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Total Time: ${totalMinutes / 60}h ${totalMinutes % 60}m across ${sessions.size} sessions",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LavenderAccent
                    )
                )
            }

            items(sessions, key = { it.id }) { session ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceCard,
                    borderColor = BorderSubtle,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${session.durationMinutes} Minutes",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = AmberPrimary
                            )
                            Text(
                                text = dateFormat.format(Date(session.startTime)),
                                style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                            )
                        }

                        if (session.chapterNumbersCovered.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Covered chapters: ${session.chapterNumbersCovered.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        if (session.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = session.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
