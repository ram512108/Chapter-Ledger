package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.FuturisticPrimaryButton
import com.example.ui.components.FuturisticSecondaryButton
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderMedium
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    var showJsonImportDialog by remember { mutableStateOf(false) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    var jsonImportText by remember { mutableStateOf("") }
    var csvImportText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System & Data",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.3).sp
                        ),
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // App summary card
            GlassCard(
                backgroundColor = SurfaceCard,
                borderColor = BorderSubtle,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chapterly",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = AmberPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The non-linear, chapter-level book tracking intelligence system. Read chapters in any sequence and monitor your journey with fine-grained precision.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp
                        ),
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reading Challenge Goal
            Text(
                text = "READING CHALLENGE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(6.dp))

            GlassCard(
                backgroundColor = SurfaceCard,
                borderColor = BorderSubtle,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmberGlow,
                            border = BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = AmberPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Annual Book Target",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = "${state.annualGoal} books goal for this year",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Data Export & Backup
            Text(
                text = "BACKUP & EXPORT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(6.dp))

            GlassCard(
                backgroundColor = SurfaceCard,
                borderColor = BorderSubtle,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = LavenderAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Export Library to JSON",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Export all books, chapters, ratings, and shelves into JSON format",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    FuturisticPrimaryButton(
                        text = "Generate JSON Backup",
                        onClick = viewModel::exportToJson,
                        icon = Icons.Default.CloudUpload,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_json_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Data Import
            Text(
                text = "IMPORT DATA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(6.dp))

            GlassCard(
                backgroundColor = SurfaceCard,
                borderColor = BorderSubtle,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = LavenderAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Import from Goodreads CSV",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Paste CSV export from goodreads.com to import your shelves",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FuturisticSecondaryButton(
                        text = "Paste Goodreads CSV",
                        onClick = { showCsvImportDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_csv_button")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Restore from JSON Backup",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Restore previous Chapterly library backups",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FuturisticSecondaryButton(
                        text = "Paste JSON Backup",
                        onClick = { showJsonImportDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_json_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Data and attribution
            Text(
                text = "PRIVACY & ATTRIBUTION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(6.dp))
            GlassCard(
                backgroundColor = SurfaceCard,
                borderColor = BorderSubtle,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Privacy",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your library, notes, quotes, reading history, and goals are stored locally on this device. Book searches and ISBN lookups use Open Library.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Book metadata and cover images may be provided by Open Library. See the project privacy policy for details.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Demo data reload
            Text(
                text = "SAMPLE DATA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(6.dp))

            GlassCard(
                backgroundColor = SurfaceCard,
                borderColor = BorderSubtle,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Reload Demo Library",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Includes Ted Chiang's Exhalation with non-linear chapters read, Dune, Atomic Habits, and reading sessions",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FuturisticSecondaryButton(
                        text = "Reload Demo Data",
                        onClick = viewModel::restoreSampleData,
                        icon = Icons.Default.RestartAlt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reload_sample_data_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    val clipboardManager = LocalClipboardManager.current

    // Message alert dialog
    state.message?.let { msg ->
        BasicAlertDialog(
            onDismissRequest = viewModel::clearMessage,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 580.dp)
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceElevated)
                .border(BorderStroke(1.dp, BorderMedium), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Chapterly",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                state.exportedJson?.let { json ->
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = json,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    state.exportedJson?.let { json ->
                        FuturisticSecondaryButton(
                            text = "Copy JSON",
                            onClick = {
                                clipboardManager.setText(AnnotatedString(json))
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    FuturisticPrimaryButton(
                        text = "OK",
                        onClick = viewModel::clearMessage
                    )
                }
            }
        }
    }

    // CSV Import Dialog
    if (showCsvImportDialog) {
        BasicAlertDialog(
            onDismissRequest = { showCsvImportDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 600.dp)
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceElevated)
                .border(BorderStroke(1.dp, BorderMedium), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Import Goodreads CSV",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Paste the text contents of your Goodreads export CSV file here:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = csvImportText,
                    onValueChange = { csvImportText = it },
                    placeholder = { Text("\"Book Id\",\"Title\",\"Author\"...", color = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 250.dp)
                        .testTag("csv_input_field"),
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
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticSecondaryButton(
                        text = "Cancel",
                        onClick = { showCsvImportDialog = false },
                        modifier = Modifier.weight(1f)
                    )
                    FuturisticPrimaryButton(
                        text = "Import Books",
                        onClick = {
                            viewModel.importGoodreadsCsv(csvImportText)
                            showCsvImportDialog = false
                            csvImportText = ""
                        },
                        enabled = csvImportText.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // JSON Import Dialog
    if (showJsonImportDialog) {
        BasicAlertDialog(
            onDismissRequest = { showJsonImportDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 600.dp)
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceElevated)
                .border(BorderStroke(1.dp, BorderMedium), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Restore JSON Backup",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Paste your exported Chapterly JSON backup string:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = jsonImportText,
                    onValueChange = { jsonImportText = it },
                    placeholder = { Text("{\"version\": 1, \"books\": [...] }", color = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 250.dp)
                        .testTag("json_input_field"),
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
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticSecondaryButton(
                        text = "Cancel",
                        onClick = { showJsonImportDialog = false },
                        modifier = Modifier.weight(1f)
                    )
                    FuturisticPrimaryButton(
                        text = "Restore",
                        onClick = {
                            viewModel.importFromJson(jsonImportText)
                            showJsonImportDialog = false
                            jsonImportText = ""
                        },
                        enabled = jsonImportText.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
