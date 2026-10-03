package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveReadingSession
import com.example.model.Book
import com.example.model.ReadingSession
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BorderMedium
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingSessionSheet(
    book: Book,
    activeSession: ActiveReadingSession,
    onDismiss: () -> Unit,
    onToggleTimer: () -> Unit,
    onResetSession: () -> Unit,
    onUpdateDraft: (chapters: String, pages: String, notes: String) -> Unit,
    onFinishSession: (ReadingSession) -> Unit
) {
    var chaptersCoveredInput by remember(activeSession.id) { mutableStateOf(activeSession.chaptersCovered) }
    var pagesCoveredInput by remember(activeSession.id) { mutableStateOf(activeSession.pagesCovered) }
    var sessionNotes by remember(activeSession.id) { mutableStateOf(activeSession.notes) }
    var showResetConfirmation by remember { mutableStateOf(false) }

    // Sync if external session draft updates (e.g. from background restore)
    LaunchedEffect(activeSession.chaptersCovered, activeSession.pagesCovered, activeSession.notes) {
        if (chaptersCoveredInput != activeSession.chaptersCovered && activeSession.chaptersCovered.isNotEmpty()) {
            chaptersCoveredInput = activeSession.chaptersCovered
        }
        if (pagesCoveredInput != activeSession.pagesCovered && activeSession.pagesCovered.isNotEmpty()) {
            pagesCoveredInput = activeSession.pagesCovered
        }
        if (sessionNotes != activeSession.notes && activeSession.notes.isNotEmpty()) {
            sessionNotes = activeSession.notes
        }
    }

    // Accurate real-time timer calculation from timestamps
    val currentTimeMillis by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = activeSession.isRunning,
        key2 = activeSession.lastResumeTime
    ) {
        while (activeSession.isRunning) {
            delay(500L)
            value = System.currentTimeMillis()
        }
    }

    val elapsedSeconds = activeSession.calculateElapsedSeconds(currentTimeMillis)
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundSecondary,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AmberGlow,
                        border = BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.5f)),
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
                        Text(
                            text = "Active Reading Session",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = if (activeSession.isRunning) "Running in background" else "Paused",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (activeSession.isRunning) AmberPrimary else TextTertiary
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close (Keep session running)",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Reading: ${book.title}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Big Timer Card
            GlassCard(
                backgroundColor = SurfaceCard,
                borderColor = BorderSubtle,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        ),
                        color = AmberPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = LavenderAccent,
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(
                                onClick = onToggleTimer,
                                modifier = Modifier.testTag("session_timer_toggle")
                            ) {
                                Icon(
                                    imageVector = if (activeSession.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (activeSession.isRunning) "Pause" else "Start",
                                    tint = Color(0xFF07090B),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        FuturisticSecondaryButton(
                            text = "Reset",
                            onClick = {
                                showResetConfirmation = true
                            },
                            icon = Icons.Default.Refresh
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs for chapters / pages covered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = chaptersCoveredInput,
                    onValueChange = {
                        chaptersCoveredInput = it
                        onUpdateDraft(it, pagesCoveredInput, sessionNotes)
                    },
                    label = { Text("Chapters (e.g. 1, 3)", color = TextSecondary) },
                    placeholder = { Text("Numbers", color = TextTertiary) },
                    modifier = Modifier.weight(1f).testTag("session_chapters_input"),
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
                    value = pagesCoveredInput,
                    onValueChange = {
                        pagesCoveredInput = it
                        onUpdateDraft(chaptersCoveredInput, it, sessionNotes)
                    },
                    label = { Text("Pages Read", color = TextSecondary) },
                    placeholder = { Text("e.g. 25", color = TextTertiary) },
                    modifier = Modifier.weight(1f).testTag("session_pages_input"),
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = sessionNotes,
                onValueChange = {
                    sessionNotes = it
                    onUpdateDraft(chaptersCoveredInput, pagesCoveredInput, it)
                },
                label = { Text("Session Notes", color = TextSecondary) },
                placeholder = { Text("What happened during this reading session?", color = TextTertiary) },
                modifier = Modifier.fillMaxWidth().testTag("session_notes_input"),
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
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(20.dp))

            val durationMin = maxOf(1, (elapsedSeconds / 60).toInt())

            FuturisticPrimaryButton(
                text = "Finish & Log Session ($durationMin min)",
                onClick = {
                    val chapters = chaptersCoveredInput.split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                    val pages = pagesCoveredInput.toIntOrNull() ?: 0

                    val finalSession = ReadingSession(
                        id = activeSession.id,
                        bookId = book.id,
                        bookTitle = book.title,
                        chapterNumbersCovered = chapters,
                        startTime = activeSession.startTime,
                        endTime = System.currentTimeMillis(),
                        durationMinutes = durationMin,
                        pagesCovered = pages,
                        notes = sessionNotes.trim()
                    )
                    onFinishSession(finalSession)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_reading_session_button")
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Reset Confirmation Dialog
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = {
                Text(
                    text = "Reset Reading Session?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to reset this reading session? The active timer progress, chapters entered, and session notes will be discarded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            containerColor = SurfaceElevated,
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmation = false
                        onResetSession()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF43F5E))
                ) {
                    Text("Reset Session", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirmation = false }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
