package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.model.Chapter
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BackgroundDark
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailDialog(
    chapter: Chapter,
    onDismiss: () -> Unit,
    onSave: (customTitle: String, notes: String, quotes: List<String>, rating: Float, pageCount: Int) -> Unit,
    onToggleRead: () -> Unit,
    onTogglePin: () -> Unit
) {
    var title by remember(chapter.id) { mutableStateOf(chapter.customTitle) }
    var pageCountText by remember(chapter.id) { mutableStateOf(if (chapter.pageCount > 0) chapter.pageCount.toString() else "") }
    var notes by remember(chapter.id) { mutableStateOf(chapter.notes) }
    var rating by remember(chapter.id) { mutableFloatStateOf(chapter.rating) }
    val quotes = remember(chapter.id) { mutableStateListOf<String>().apply { addAll(chapter.highlightedQuotes) } }
    var newQuoteText by remember { mutableStateOf("") }
    var showAddQuoteInput by remember { mutableStateOf(false) }
    var editingQuoteIndex by remember { mutableStateOf<Int?>(null) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .widthIn(max = 620.dp)
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
            // Header: Chapter number & close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chapter ${chapter.number}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = TextPrimary
                    )
                    if (chapter.isRead && chapter.readingOrderIndex != null) {
                        Text(
                            text = "Completed #${chapter.readingOrderIndex} in reading sequence",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ChapterReadGreen
                            )
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
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

            // Body (Scrollable)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Pin & Read Status quick action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mark read/unread chip
                    Surface(
                        onClick = onToggleRead,
                        shape = RoundedCornerShape(12.dp),
                        color = if (chapter.isRead) ChapterReadGreenGlow else SurfaceCard,
                        border = BorderStroke(1.dp, if (chapter.isRead) ChapterReadGreen.copy(alpha = 0.6f) else BorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("mark_read_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (chapter.isRead) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (chapter.isRead) "Mark Unread" else "Mark Read",
                                tint = if (chapter.isRead) ChapterReadGreen else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (chapter.isRead) "Mark Unread" else "Mark Read",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (chapter.isRead) ChapterReadGreen else TextPrimary
                            )
                        }
                    }

                    // Pin next chip
                    Surface(
                        onClick = onTogglePin,
                        shape = RoundedCornerShape(12.dp),
                        color = if (chapter.isPinnedNext) AmberGlow else SurfaceCard,
                        border = BorderStroke(1.dp, if (chapter.isPinnedNext) AmberPrimary.copy(alpha = 0.6f) else BorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("pin_next_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (chapter.isPinnedNext) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (chapter.isPinnedNext) "Unpin Next" else "Pin Next",
                                tint = if (chapter.isPinnedNext) AmberPrimary else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (chapter.isPinnedNext) "Pinned Next" else "Pin Next",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (chapter.isPinnedNext) AmberPrimary else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Custom Title / Story Name", color = TextSecondary) },
                    placeholder = { Text("e.g. The Merchant and the Alchemist's Gate", color = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chapter_title_input"),
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

                // Page Count input
                OutlinedTextField(
                    value = pageCountText,
                    onValueChange = { pageCountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Page Count", color = TextSecondary) },
                    placeholder = { Text("e.g. 24", color = TextTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chapter_page_count_input"),
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

                // Star Rating
                Text(
                    text = "Chapter Rating",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        val starVal = star.toFloat()
                        val icon = when {
                            rating >= starVal -> Icons.Default.Star
                            rating >= starVal - 0.5f -> Icons.Default.StarHalf
                            else -> Icons.Default.StarBorder
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "$star Stars",
                            tint = if (rating >= starVal - 0.5f) AmberPrimary else TextTertiary,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    rating = if (rating == starVal) 0f else starVal
                                }
                                .padding(2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (rating > 0f) {
                        Text(
                            text = "$rating / 5.0",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = AmberPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Personal Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & Reflections", color = TextSecondary) },
                    placeholder = { Text("Key insights, plot twists, character moments...", color = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chapter_notes_input"),
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
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Highlighted Quotes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Highlighted Quotes (${quotes.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    )
                    TextButton(onClick = { showAddQuoteInput = !showAddQuoteInput }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = LavenderAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Quote", color = LavenderAccent)
                    }
                }

                if (showAddQuoteInput) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, if (editingQuoteIndex != null) AmberPrimary else LavenderAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (editingQuoteIndex != null) "Edit Quote" else "Add New Quote",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (editingQuoteIndex != null) AmberPrimary else LavenderAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = newQuoteText,
                                onValueChange = { newQuoteText = it },
                                label = { Text("Quote Text", color = TextSecondary) },
                                placeholder = { Text("Enter a memorable line or passage...", color = TextTertiary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceElevated,
                                    unfocusedContainerColor = SurfaceElevated,
                                    focusedBorderColor = LavenderAccent,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                minLines = 2
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    showAddQuoteInput = false
                                    newQuoteText = ""
                                    editingQuoteIndex = null
                                }) {
                                    Text("Cancel", color = TextTertiary)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(
                                    onClick = {
                                        if (newQuoteText.isNotBlank()) {
                                            val currentIdx = editingQuoteIndex
                                            if (currentIdx != null && currentIdx in quotes.indices) {
                                                quotes[currentIdx] = newQuoteText.trim()
                                            } else {
                                                quotes.add(newQuoteText.trim())
                                            }
                                            newQuoteText = ""
                                            editingQuoteIndex = null
                                            showAddQuoteInput = false
                                        }
                                    },
                                    enabled = newQuoteText.isNotBlank(),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = LavenderAccent,
                                        contentColor = Color(0xFF07090B)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (editingQuoteIndex != null) "Update" else "Add", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                quotes.forEachIndexed { idx, quote ->
                    Card(
                        onClick = {
                            editingQuoteIndex = idx
                            newQuoteText = quote
                            showAddQuoteInput = true
                        },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, if (editingQuoteIndex == idx) AmberPrimary else BorderSubtle),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "“$quote”",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (editingQuoteIndex == idx) {
                                        editingQuoteIndex = null
                                        showAddQuoteInput = false
                                        newQuoteText = ""
                                    }
                                    quotes.removeAt(idx)
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Quote",
                                    tint = Color(0xFFF43F5E),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Footer: Cancel & Save buttons
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
                    text = "Save",
                    onClick = {
                        val parsedPages = pageCountText.toIntOrNull() ?: 0
                        onSave(title, notes, quotes.toList(), rating, parsedPages)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_chapter_details_button")
                )
            }
        }
    }
}
