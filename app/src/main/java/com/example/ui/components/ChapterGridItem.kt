package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Chapter
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberPrimary
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

@Composable
fun ChapterGridItem(
    chapter: Chapter,
    onToggleRead: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = chapter.isRead
    val isPinned = chapter.isPinnedNext

    val targetContainerColor = when {
        isCompleted -> Color(0xFF0F1815) // subtle emerald undertone
        isPinned -> Color(0xFF191710)    // subtle amber undertone
        else -> SurfaceCard
    }

    val targetBorderColor = when {
        isPinned -> AmberPrimary.copy(alpha = 0.8f)
        isCompleted -> ChapterReadGreen.copy(alpha = 0.6f)
        else -> BorderSubtle
    }

    val containerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(180),
        label = "chapterContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(180),
        label = "chapterBorder"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(13.dp))
            .clickable(onClick = onOpenDetails)
            .testTag("chapter_item_${chapter.number}"),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isPinned) 1.5.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 11.dp, vertical = 10.dp)
        ) {
            // Header Row: Chapter Number/Status Badge (click toggles read) + Non-linear Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chapter Number / Status Control Badge
                Surface(
                    onClick = onToggleRead,
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isCompleted -> ChapterReadGreen
                        isPinned -> AmberPrimary
                        else -> SurfaceElevated
                    },
                    border = BorderStroke(
                        0.5.dp,
                        when {
                            isCompleted -> ChapterReadGreen
                            isPinned -> AmberPrimary
                            else -> BorderSubtle
                        }
                    ),
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("chapter_status_badge_${chapter.number}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Mark as unread",
                                tint = Color(0xFF07090B),
                                modifier = Modifier.size(17.dp)
                            )
                        } else {
                            Text(
                                text = String.format("%02d", chapter.number),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isPinned) Color(0xFF07090B) else TextPrimary
                            )
                        }
                    }
                }

                // Non-linear status badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCompleted && chapter.readingOrderIndex != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ChapterReadGreenGlow,
                            border = BorderStroke(0.5.dp, ChapterReadGreen.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "#${chapter.readingOrderIndex}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = ChapterReadGreen
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isPinned) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberGlow,
                            border = BorderStroke(0.5.dp, AmberPrimary.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = AmberPrimary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "NEXT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = AmberPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title (occupies middle space so titles align across cards)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = chapter.displayTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isCompleted || isPinned) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimary
                )
            }

            // Badges row for Notes, Quotes, Rating, Page count with reserved slot height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (chapter.notes.isNotBlank() || chapter.highlightedQuotes.isNotEmpty() || chapter.rating > 0f || chapter.pageCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (chapter.pageCount > 0) {
                            Text(
                                text = "${chapter.pageCount}p",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = AmberPrimary
                            )
                        }
                        if (chapter.rating > 0f) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AmberPrimary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${chapter.rating}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                )
                            }
                        }
                        if (chapter.highlightedQuotes.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = LavenderAccent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(1.dp))
                                Text(
                                    text = "${chapter.highlightedQuotes.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                )
                            }
                        }
                        if (chapter.notes.isNotBlank()) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    color = TextTertiary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
