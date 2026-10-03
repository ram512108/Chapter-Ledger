package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHigherElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TimelineFilterOption(val label: String) {
    ALL("All Chapters"),
    UNREAD("Pending"),
    COMPLETED("Completed"),
    READING_ORDER("Reading Order")
}

/**
 * High-fidelity timeline UI component that visualizes a book's chapters sequentially or by reading order,
 * with continuous vertical connecting lines, interactive completion status toggles, and detail navigation.
 */
@Composable
fun ChapterTimelineView(
    chapters: List<Chapter>,
    onToggleRead: (String) -> Unit,
    onChapterClick: (Chapter) -> Unit,
    modifier: Modifier = Modifier,
    showSummaryHeader: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(top = 12.dp, bottom = 64.dp)
) {
    var selectedFilter by remember { mutableStateOf(TimelineFilterOption.ALL) }

    val completedCount = chapters.count { it.isRead }
    val totalCount = chapters.size
    val progressPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    val filteredChapters = when (selectedFilter) {
        TimelineFilterOption.ALL -> chapters.sortedBy { it.number }
        TimelineFilterOption.UNREAD -> chapters.filter { !it.isRead }.sortedBy { it.number }
        TimelineFilterOption.COMPLETED -> chapters.filter { it.isRead }.sortedBy { it.number }
        TimelineFilterOption.READING_ORDER -> chapters.filter { it.isRead && it.readingOrderIndex != null }
            .sortedBy { it.readingOrderIndex }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("chapter_timeline_view")
    ) {
        if (showSummaryHeader && chapters.isNotEmpty()) {
            TimelineHeader(
                completedCount = completedCount,
                totalCount = totalCount,
                progressPercent = progressPercent,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        }

        if (filteredChapters.isEmpty()) {
            TimelineEmptyState(selectedFilter = selectedFilter)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = contentPadding
            ) {
                itemsIndexed(
                    items = filteredChapters,
                    key = { _, chapter -> chapter.id }
                ) { index, chapter ->
                    val isFirst = index == 0
                    val isLast = index == filteredChapters.size - 1
                    val prevChapter = if (!isFirst) filteredChapters[index - 1] else null
                    val nextChapter = if (!isLast) filteredChapters[index + 1] else null

                    TimelineItemRow(
                        chapter = chapter,
                        isFirst = isFirst,
                        isLast = isLast,
                        isPrevCompleted = prevChapter?.isRead == true,
                        isNextCompleted = nextChapter?.isRead == true,
                        filterOption = selectedFilter,
                        onToggleRead = { onToggleRead(chapter.id) },
                        onChapterClick = { onChapterClick(chapter) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineHeader(
    completedCount: Int,
    totalCount: Int,
    progressPercent: Int,
    selectedFilter: TimelineFilterOption,
    onFilterSelected: (TimelineFilterOption) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Stats Overview Card
        GlassCard(
            backgroundColor = SurfaceElevated,
            borderColor = BorderSubtle,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = LavenderGlow,
                        border = BorderStroke(1.dp, LavenderAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = LavenderAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Chapter Timeline",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "$completedCount of $totalCount completed ($progressPercent%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (completedCount == totalCount && totalCount > 0) ChapterReadGreenGlow else AmberGlow,
                    border = BorderStroke(
                        1.dp,
                        if (completedCount == totalCount && totalCount > 0) ChapterReadGreen.copy(alpha = 0.6f) else AmberPrimary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = if (completedCount == totalCount && totalCount > 0) "100% COMPLETE" else "$progressPercent% READ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        ),
                        color = if (completedCount == totalCount && totalCount > 0) ChapterReadGreen else AmberPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TimelineFilterOption.entries.forEach { option ->
                val isSelected = selectedFilter == option
                Surface(
                    onClick = { onFilterSelected(option) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) LavenderAccent else SurfaceCard,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) LavenderAccent else BorderSubtle
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) Color(0xFF07090B) else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    chapter: Chapter,
    isFirst: Boolean,
    isLast: Boolean,
    isPrevCompleted: Boolean,
    isNextCompleted: Boolean,
    filterOption: TimelineFilterOption,
    onToggleRead: () -> Unit,
    onChapterClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val isCompleted = chapter.isRead
    val isPinned = chapter.isPinnedNext

    // Smooth animation on toggle
    val nodeScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "nodeScale"
    )

    val nodeBgColor by animateColorAsState(
        targetValue = when {
            isCompleted -> ChapterReadGreen
            isPinned -> AmberPrimary
            else -> SurfaceElevated
        },
        animationSpec = tween(220),
        label = "nodeBgColor"
    )

    val nodeBorderColor by animateColorAsState(
        targetValue = when {
            isCompleted -> ChapterReadGreen
            isPinned -> AmberPrimary
            else -> BorderMedium
        },
        animationSpec = tween(220),
        label = "nodeBorderColor"
    )

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            isPinned -> AmberPrimary.copy(alpha = 0.8f)
            isCompleted -> ChapterReadGreen.copy(alpha = 0.4f)
            else -> BorderSubtle
        },
        animationSpec = tween(200),
        label = "cardBorderColor"
    )

    val cardBgColor by animateColorAsState(
        targetValue = when {
            isPinned -> Color(0xFF19160E)
            isCompleted -> Color(0xFF0E1713)
            else -> SurfaceCard
        },
        animationSpec = tween(200),
        label = "cardBgColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .testTag("timeline_item_${chapter.number}")
    ) {
        // Left Track: Connecting Lines and Node Badge
        Column(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Connector
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(14.dp)
                        .background(
                            if (isPrevCompleted && isCompleted) ChapterReadGreen.copy(alpha = 0.8f)
                            else BorderMedium
                        )
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Interactive Node Circle (Toggles completion status)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(nodeScale)
                    .clip(CircleShape)
                    .clickable(
                        role = Role.Checkbox,
                        onClick = onToggleRead
                    )
                    .testTag("timeline_node_toggle_${chapter.number}"),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring / glow
                Surface(
                    shape = CircleShape,
                    color = nodeBgColor,
                    border = BorderStroke(1.5.dp, nodeBorderColor),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when {
                            isCompleted -> {
                                if (filterOption == TimelineFilterOption.READING_ORDER && chapter.readingOrderIndex != null) {
                                    Text(
                                        text = "#${chapter.readingOrderIndex}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = Color(0xFF07090B)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed (Click to mark unread)",
                                        tint = Color(0xFF07090B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            isPinned -> {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Pinned next (Click to mark completed)",
                                    tint = Color(0xFF07090B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            else -> {
                                Text(
                                    text = String.format("%02d", chapter.number),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Connector
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .weight(1f)
                        .background(
                            if (isCompleted && isNextCompleted) ChapterReadGreen.copy(alpha = 0.8f)
                            else BorderMedium
                        )
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Right Track: Chapter Content Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onChapterClick)
                .testTag("timeline_chapter_card_${chapter.number}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Top Row: Title, Pin Badge, and Quick Toggle Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chapter.displayTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCompleted || isPinned) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 14.5.sp
                            ),
                            color = if (isCompleted) TextPrimary else if (isPinned) AmberPrimary else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isPinned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberGlow,
                                border = BorderStroke(0.5.dp, AmberPrimary)
                            ) {
                                Text(
                                    text = "NEXT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = AmberPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // One-tap Completion Status Toggle Pill
                    Surface(
                        onClick = onToggleRead,
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCompleted) ChapterReadGreenGlow else SurfaceElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isCompleted) ChapterReadGreen.copy(alpha = 0.7f) else BorderMedium
                        ),
                        modifier = Modifier.testTag("timeline_card_toggle_${chapter.number}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (isCompleted) "Mark as Unread" else "Mark as Read",
                                tint = if (isCompleted) ChapterReadGreen else TextTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isCompleted) "Done" else "Read",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = if (isCompleted) ChapterReadGreen else TextSecondary
                            )
                        }
                    }
                }

                // Metadata details
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (chapter.pageCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${chapter.pageCount} pp",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextTertiary
                            )
                        }
                    }

                    if (isCompleted && chapter.dateRead != null) {
                        Text(
                            text = "Read on ${dateFormat.format(Date(chapter.dateRead))}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = ChapterReadGreen.copy(alpha = 0.85f)
                        )
                    }

                    if (chapter.rating > 0f) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", chapter.rating),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = AmberPrimary
                            )
                        }
                    }

                    if (chapter.highlightedQuotes.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = LavenderAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${chapter.highlightedQuotes.size} quote${if (chapter.highlightedQuotes.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = LavenderAccent
                            )
                        }
                    }
                }

                if (chapter.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = chapter.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineEmptyState(selectedFilter: TimelineFilterOption) {
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
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = LavenderAccent,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (selectedFilter) {
                TimelineFilterOption.ALL -> "No Chapters in Timeline"
                TimelineFilterOption.UNREAD -> "All Chapters Completed!"
                TimelineFilterOption.COMPLETED -> "No Completed Chapters Yet"
                TimelineFilterOption.READING_ORDER -> "No Reading Order Sequence"
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (selectedFilter) {
                TimelineFilterOption.ALL -> "Add chapters to this book to begin mapping your reading timeline."
                TimelineFilterOption.UNREAD -> "You've marked all chapters in this book as read."
                TimelineFilterOption.COMPLETED -> "Tap on the timeline node or 'Read' button on any chapter to mark it as read."
                TimelineFilterOption.READING_ORDER -> "As you mark chapters read in any order, your custom non-linear reading order will show here."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
