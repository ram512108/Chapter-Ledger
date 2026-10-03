package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.domain.ChapterParser
import com.example.domain.ParsedChapter
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderMedium
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkChaptersDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (List<ParsedChapter>) -> Unit
) {
    var rawText by remember {
        mutableStateOf(initialText)
    }

    val parsedChapters = remember(rawText) {
        ChapterParser.parseBulkText(rawText)
    }

    val totalPagesSum = remember(parsedChapters) {
        parsedChapters.sumOf { it.pageCount }
    }

    val errors = remember(parsedChapters) {
        parsedChapters.filter { it.hasError }
    }

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
            Text(
                text = "Bulk Chapters & Pages",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Enter each chapter as chapter_title|page_count (e.g. Chapter 1|24). If page count is omitted, it defaults to 0. Existing progress and notes will be preserved.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                label = {
                    Text(
                        text = "Chapters (${parsedChapters.size} detected · $totalPagesSum pages)",
                        color = TextSecondary
                    )
                },
                placeholder = {
                    Text(
                        "Chapter 1|24\nChapter 2|18\nChapter 3|32\nChapter 4|15",
                        color = TextTertiary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 280.dp)
                    .testTag("bulk_chapters_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = LavenderAccent,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                maxLines = 100
            )

            if (errors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ ${errors.first().errorMessage}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, color = Color(0xFFF43F5E))
                )
            } else if (parsedChapters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓ ${parsedChapters.size} chapters ready to sync",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = AmberPrimary)
                    )
                    if (totalPagesSum > 0) {
                        Text(
                            text = "$totalPagesSum total chapter pages",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = TextSecondary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

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
                    text = "Apply (${parsedChapters.size})",
                    onClick = {
                        onSave(parsedChapters)
                        onDismiss()
                    },
                    enabled = parsedChapters.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_bulk_chapters_button")
                )
            }
        }
    }
}

