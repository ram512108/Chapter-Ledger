package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

data class ChapterOption(
    val id: String,
    val number: Int,
    val title: String
) {
    val displayLabel: String
        get() = if (title.isNotBlank()) "Ch. $number: $title" else "Chapter $number"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteEditDialog(
    initialQuote: String,
    initialNotes: String = "",
    initialPageNumber: Int = 0,
    initialIsFavorite: Boolean = false,
    bookTitle: String,
    author: String = "",
    currentChapterId: String,
    availableChapters: List<ChapterOption> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (quoteText: String, notes: String, pageNumber: Int, targetChapterId: String, isFavorite: Boolean) -> Unit
) {
    var quoteText by remember { mutableStateOf(initialQuote) }
    var notesText by remember { mutableStateOf(initialNotes) }
    var pageText by remember { mutableStateOf(if (initialPageNumber > 0) initialPageNumber.toString() else "") }
    var isFavorite by remember { mutableStateOf(initialIsFavorite) }
    var selectedChapterId by remember { mutableStateOf(currentChapterId) }
    var isChapterMenuExpanded by remember { mutableStateOf(false) }

    val selectedChapter = availableChapters.find { it.id == selectedChapterId }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("quote_edit_dialog"),
            backgroundColor = SurfaceElevated,
            borderColor = BorderSubtle,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Edit Saved Quote",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = TextPrimary
                            )
                            if (bookTitle.isNotBlank()) {
                                Text(
                                    text = "$bookTitle${if (author.isNotBlank()) " • $author" else ""}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    color = LavenderAccent,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Quote Text
                    OutlinedTextField(
                        value = quoteText,
                        onValueChange = { quoteText = it },
                        label = { Text("Quote Text", color = TextSecondary) },
                        placeholder = { Text("Enter the quote text...", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_quote_text_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = LavenderAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        minLines = 3,
                        maxLines = 8
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chapter Selector (if available)
                    if (availableChapters.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = isChapterMenuExpanded,
                            onExpandedChange = { isChapterMenuExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedChapter?.displayLabel ?: "Select Chapter",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Chapter", color = TextSecondary) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isChapterMenuExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("edit_quote_chapter_selector"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceCard,
                                    unfocusedContainerColor = SurfaceCard,
                                    focusedBorderColor = LavenderAccent,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = isChapterMenuExpanded,
                                onDismissRequest = { isChapterMenuExpanded = false }
                            ) {
                                availableChapters.forEach { chapter ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = chapter.displayLabel,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (chapter.id == selectedChapterId) AmberPrimary else TextPrimary
                                            )
                                        },
                                        onClick = {
                                            selectedChapterId = chapter.id
                                            isChapterMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Page Number & Favorite Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = pageText,
                            onValueChange = { pageText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Page Number", color = TextSecondary) },
                            placeholder = { Text("e.g. 42", color = TextTertiary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_quote_page_input"),
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

                        // Favorite toggle surface
                        Surface(
                            onClick = { isFavorite = !isFavorite },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFavorite) AmberGlow else SurfaceCard,
                            border = BorderStroke(1.dp, if (isFavorite) AmberPrimary.copy(alpha = 0.6f) else BorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("edit_quote_favorite_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavorite) Color(0xFFF43F5E) else TextTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFavorite) "Favorited" else "Favorite",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isFavorite) Color(0xFFF43F5E) else TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Personal Notes & Reflections
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notes & Reflections", color = TextSecondary) },
                        placeholder = { Text("Why this quote stood out to you...", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_quote_notes_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = LavenderAccent,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        minLines = 2,
                        maxLines = 5
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticSecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    FuturisticPrimaryButton(
                        text = "Save Quote",
                        onClick = {
                            val parsedPage = pageText.toIntOrNull() ?: 0
                            onSave(
                                quoteText.trim(),
                                notesText.trim(),
                                parsedPage,
                                selectedChapterId,
                                isFavorite
                            )
                            onDismiss()
                        },
                        enabled = quoteText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_edited_quote_button")
                    )
                }
            }
        }
    }
}
