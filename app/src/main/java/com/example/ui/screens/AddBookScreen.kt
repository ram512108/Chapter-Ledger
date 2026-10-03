package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BookFormat
import com.example.model.BookStatus
import com.example.ui.components.BarcodeScanDialog
import com.example.ui.components.BookCoverImage
import com.example.ui.components.FuturisticFilterChip
import com.example.ui.components.FuturisticPrimaryButton
import com.example.ui.components.FuturisticSecondaryButton
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.AddBookViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    viewModel: AddBookViewModel,
    onBackClick: () -> Unit,
    onBookSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showBarcodeDialog by remember { mutableStateOf(false) }

    fun launchBarcodeScanner() {
        try {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_QR_CODE
                )
                .enableAutoZoom()
                .build()

            val scanner = GmsBarcodeScanning.getClient(context, options)
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val raw = barcode.rawValue?.filter { it.isDigit() || it.equals('X', ignoreCase = true) } ?: ""
                    if (raw.isNotBlank()) {
                        Toast.makeText(context, "Scanned ISBN: $raw", Toast.LENGTH_SHORT).show()
                        viewModel.searchByIsbn(raw)
                    }
                }
                .addOnFailureListener {
                    // Fallback to in-app scanning dialog (works seamlessly in emulators & non-GMS devices)
                    showBarcodeDialog = true
                }
        } catch (e: Exception) {
            showBarcodeDialog = true
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            val msg = state.successMessage ?: if (state.isEditing) "Book updated successfully" else "Book added successfully"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onBookSaved()
        }
    }

    if (showBarcodeDialog) {
        BarcodeScanDialog(
            onDismiss = { showBarcodeDialog = false },
            onIsbnSelected = { isbn ->
                Toast.makeText(context, "ISBN Selected: $isbn", Toast.LENGTH_SHORT).show()
                viewModel.searchByIsbn(isbn)
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag("add_book_screen"),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Edit Book Details" else "Add to Ledger",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("add_book_back_button")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveBook,
                        enabled = !state.isSaving,
                        modifier = Modifier.testTag("top_bar_save_book_button")
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = LavenderAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (state.isEditing) "Save" else "Add",
                                color = if (state.title.isNotBlank()) LavenderAccent else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = BackgroundDark,
                contentColor = LavenderAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
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
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    text = {
                        Text(
                            text = "Search Online",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (state.selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.5.sp
                            ),
                            color = if (state.selectedTab == 0) LavenderAccent else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    text = {
                        Text(
                            text = if (state.isEditing) "Book Details" else "Manual Entry",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (state.selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.5.sp
                            ),
                            color = if (state.selectedTab == 1) LavenderAccent else TextSecondary
                        )
                    }
                )
            }

            if (state.selectedTab == 0) {
                OnlineSearchTab(
                    viewModel = viewModel,
                    onScanBarcode = ::launchBarcodeScanner,
                    onOpenBarcodeDialog = { showBarcodeDialog = true }
                )
            } else {
                ManualEntryTab(
                    viewModel = viewModel,
                    onScanBarcode = ::launchBarcodeScanner
                )
            }
        }
    }
}

@Composable
private fun OnlineSearchTab(
    viewModel: AddBookViewModel,
    onScanBarcode: () -> Unit,
    onOpenBarcodeDialog: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Quick Barcode Scan Hero Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FuturisticPrimaryButton(
                text = "Scan ISBN Barcode",
                onClick = onScanBarcode,
                icon = Icons.Default.QrCodeScanner,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scan_isbn_barcode_button")
            )

            FuturisticSecondaryButton(
                text = "Samples",
                onClick = onOpenBarcodeDialog,
                modifier = Modifier
                    .width(95.dp)
                    .testTag("barcode_samples_button")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = { Text("Search title, author, or ISBN...", color = TextTertiary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_open_library_input"),
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onScanBarcode) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        tint = LavenderAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = LavenderAccent,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AmberPrimary)
            }
        } else if (state.searchError != null) {
            Text(
                text = state.searchError ?: "",
                color = Color(0xFFF43F5E),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp)
            )
        } else if (state.searchResults.isEmpty() && state.searchQuery.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "No results found on Open Library for '${state.searchQuery}'.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                FuturisticSecondaryButton(
                    text = "Switch to Form Entry",
                    onClick = {
                        viewModel.setTab(1)
                        viewModel.setManualEntrySubTab(0)
                    },
                    modifier = Modifier.testTag("switch_to_manual_form_button")
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.searchResults) { result ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectSearchResult(result) }
                            .testTag("search_result_${result.title}"),
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = SurfaceCard,
                        borderColor = BorderSubtle
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BookCoverImage(
                                coverUrl = result.coverImageUrl,
                                title = result.title,
                                author = result.author,
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(75.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = result.author,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = TextSecondary
                                )
                                if (result.estimatedPages > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${result.estimatedPages} pages • ~${(result.estimatedPages / 22).coerceIn(5, 50)} chapters",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = AmberPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            FuturisticSecondaryButton(
                                text = "Add",
                                onClick = { viewModel.quickAddBook(result) },
                                modifier = Modifier.height(34.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualEntryTab(
    viewModel: AddBookViewModel,
    onScanBarcode: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Sub-tabs: Form Entry vs JSON Import
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val isForm = state.manualEntrySubTab == 0
            Surface(
                onClick = { viewModel.setManualEntrySubTab(0) },
                shape = RoundedCornerShape(8.dp),
                color = if (isForm) LavenderAccent else Color.Transparent,
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Form Entry",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isForm) Color.Black else TextSecondary
                    )
                }
            }
            Surface(
                onClick = { viewModel.setManualEntrySubTab(1) },
                shape = RoundedCornerShape(8.dp),
                color = if (!isForm) LavenderAccent else Color.Transparent,
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "JSON Import",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (!isForm) Color.Black else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (state.manualEntrySubTab == 1) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Paste Book JSON",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Import books, chapters, and metadata directly using valid JSON.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = state.jsonImportInput,
                    onValueChange = viewModel::onJsonInputChanged,
                    placeholder = { Text("{\n  \"title\": \"Book Title\",\n  \"author\": \"Author Name\",\n  \"chapters\": [{\"title\": \"Ch 1\"}]\n}", color = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("json_import_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = LavenderAccent,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                FuturisticPrimaryButton(
                    text = "Validate & Preview JSON",
                    onClick = viewModel::validateAndPreviewJson,
                    modifier = Modifier.fillMaxWidth().testTag("validate_json_button")
                )

                if (state.jsonImportError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = state.jsonImportError ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFF43F5E), fontWeight = FontWeight.SemiBold)
                    )
                }

                if (state.jsonPreviewBook != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Preview:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = SurfaceCard,
                        borderColor = LavenderAccent.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Text(
                                text = state.jsonPreviewBook?.title ?: "",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Author: ${state.jsonPreviewBook?.author}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Chapters: ${state.jsonPreviewBook?.totalChapters} | Pages: ${state.jsonPreviewBook?.totalPages} | Format: ${state.jsonPreviewBook?.format?.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberPrimary
                            )
                            if (state.jsonPreviewChapters.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Chapters: ${state.jsonPreviewChapters.take(5).joinToString(", ") { if (it.pageCount > 0) "${it.title} (${it.pageCount}p)" else it.title }}${if (state.jsonPreviewChapters.size > 5) "..." else ""}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FuturisticPrimaryButton(
                        text = "Import Book into Ledger",
                        onClick = viewModel::importFromJson,
                        modifier = Modifier.fillMaxWidth().testTag("import_json_button")
                    )
                }
            }
        } else {
            if (state.formError != null) {
                Surface(
                    color = Color(0xFF3B1824),
                    border = BorderStroke(1.dp, Color(0xFFF43F5E)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.formError ?: "",
                            color = Color(0xFFFFB4C0),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    }
                }
            }

            // ISBN with Scan Barcode Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            OutlinedTextField(
                value = state.isbn,
                onValueChange = viewModel::onIsbnChange,
                label = { Text("ISBN (Barcode / Optional)", color = TextSecondary) },
                placeholder = { Text("e.g. 9780141439518", color = TextTertiary) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("book_isbn_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Surface(
                onClick = onScanBarcode,
                shape = RoundedCornerShape(12.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, LavenderAccent.copy(alpha = 0.5f)),
                modifier = Modifier
                    .size(54.dp)
                    .testTag("scan_isbn_icon_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan ISBN Barcode",
                        tint = LavenderAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Book Title *", color = if (state.titleError != null) Color(0xFFF43F5E) else TextSecondary) },
            placeholder = { Text("e.g. Exhalation: Stories", color = TextTertiary) },
            isError = state.titleError != null,
            supportingText = state.titleError?.let {
                { Text(it, color = Color(0xFFF43F5E), fontSize = 12.sp) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("book_title_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = LavenderAccent,
                unfocusedBorderColor = BorderSubtle,
                errorBorderColor = Color(0xFFF43F5E),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = state.author,
            onValueChange = viewModel::onAuthorChange,
            label = { Text("Author *", color = if (state.authorError != null) Color(0xFFF43F5E) else TextSecondary) },
            placeholder = { Text("e.g. Ted Chiang", color = TextTertiary) },
            isError = state.authorError != null,
            supportingText = state.authorError?.let {
                { Text(it, color = Color(0xFFF43F5E), fontSize = 12.sp) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("book_author_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = LavenderAccent,
                unfocusedBorderColor = BorderSubtle,
                errorBorderColor = Color(0xFFF43F5E),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = if (state.totalChapters > 0) state.totalChapters.toString() else "",
                onValueChange = { viewModel.onChaptersChange(it.toIntOrNull() ?: 0) },
                label = { Text("Chapters *", color = if (state.chaptersError != null) Color(0xFFF43F5E) else TextSecondary) },
                placeholder = { Text("10", color = TextTertiary) },
                isError = state.chaptersError != null,
                supportingText = state.chaptersError?.let {
                    { Text(it, color = Color(0xFFF43F5E), fontSize = 11.sp) }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("book_chapters_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = BorderSubtle,
                    errorBorderColor = Color(0xFFF43F5E),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = if (state.totalPages > 0) state.totalPages.toString() else "",
                onValueChange = { viewModel.onPagesChange(it.toIntOrNull() ?: 0) },
                label = { Text("Total Pages", color = TextSecondary) },
                placeholder = { Text("350", color = TextTertiary) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("book_total_pages_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = if (state.currentPage > 0) state.currentPage.toString() else "",
                onValueChange = { viewModel.onCurrentPageChange(it.toIntOrNull() ?: 0) },
                label = { Text("Current Page", color = TextSecondary) },
                placeholder = { Text("0", color = TextTertiary) },
                modifier = Modifier
                    .weight(1.1f)
                    .testTag("book_current_page_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = AmberPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.seriesName,
                onValueChange = viewModel::onSeriesNameChange,
                label = { Text("Series Name (optional)", color = TextSecondary) },
                placeholder = { Text("e.g. Foundation", color = TextTertiary) },
                modifier = Modifier
                    .weight(1.4f)
                    .testTag("book_series_name_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = state.seriesPosition,
                onValueChange = viewModel::onSeriesPositionChange,
                label = { Text("Vol #", color = TextSecondary) },
                placeholder = { Text("1", color = TextTertiary) },
                modifier = Modifier
                    .weight(0.7f)
                    .testTag("book_series_position_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = state.genresInput,
            onValueChange = viewModel::onGenresChange,
            label = { Text("Genres (comma-separated)", color = TextSecondary) },
            placeholder = { Text("Science Fiction, Anthology, Philosophy", color = TextTertiary) },
            modifier = Modifier.fillMaxWidth().testTag("book_genres_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = LavenderAccent,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Format selector
        Text(
            text = "Reading Format",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BookFormat.entries.forEach { format ->
                FuturisticFilterChip(
                    selected = state.format == format,
                    onClick = { viewModel.onFormatChange(format) },
                    label = format.displayName
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Initial Reading Status
        Text(
            text = "Initial Status",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(BookStatus.READING, BookStatus.WANT_TO_READ, BookStatus.FINISHED).forEach { status ->
                FuturisticFilterChip(
                    selected = state.status == status,
                    onClick = { viewModel.onStatusChange(status) },
                    label = status.displayName
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.coverImageUrl,
            onValueChange = viewModel::onCoverImageUrlChange,
            label = { Text("Cover Image URL (optional)", color = TextSecondary) },
            placeholder = { Text("https://...", color = TextTertiary) },
            modifier = Modifier.fillMaxWidth().testTag("book_cover_url_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = LavenderAccent,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Bulk paste chapters section
        Text(
            text = "Custom Chapters & Page Counts (Optional)",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        )
        Text(
            text = "Enter each chapter as chapter_title|page_count (e.g. Chapter 1|24). If page count is omitted, it defaults to 0.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            color = TextTertiary
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = state.bulkChapterTitlesInput,
            onValueChange = viewModel::onBulkChaptersChange,
            placeholder = { Text("Chapter 1|24\nChapter 2|18\nChapter 3|32\nChapter 4|15", color = TextTertiary) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 180.dp)
                .testTag("bulk_chapters_entry"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = LavenderAccent,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            maxLines = 50
        )

        Spacer(modifier = Modifier.height(24.dp))

        FuturisticPrimaryButton(
            text = if (state.isSaving) "Saving Book..." else if (state.isEditing) "Save Book Changes" else "Add to Library",
            onClick = viewModel::saveBook,
            enabled = !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_book_button")
        )

        Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
