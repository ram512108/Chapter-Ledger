package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HeatmapDay
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChapterReadGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HeatmapCalendar(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<HeatmapDay?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = SurfaceCard,
        borderColor = BorderSubtle
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reading Activity Heatmap",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = TextPrimary
                )
                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("Less", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
                    (0..4).forEach { lvl ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(heatmapColorForLevel(lvl))
                        )
                    }
                    Text("More", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7 rows (days of week) x columns (weeks)
            val columns = days.chunked(7)
            val numCols = columns.size.coerceAtLeast(1)
            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val labelWidth = 16.dp
                val labelSpacing = 6.dp
                val totalSpacings = 4.dp * (numCols - 1)
                val availableForCells = (maxWidth - labelWidth - labelSpacing - totalSpacings).coerceAtLeast(0.dp)
                val calculatedCellSize = (availableForCells / numCols).coerceIn(16.dp, 36.dp)
                val scrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Day of week labels
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        dayLabels.forEach { label ->
                            Box(
                                modifier = Modifier
                                    .size(width = labelWidth, height = calculatedCellSize),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = TextTertiary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Grid of weeks (evenly filling available width)
                    columns.forEach { weekDays ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            weekDays.forEach { day ->
                                val isSelected = selectedDay?.dateMillis == day.dateMillis
                                Box(
                                    modifier = Modifier
                                        .size(calculatedCellSize)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(heatmapColorForLevel(day.level))
                                        .then(
                                            if (isSelected) Modifier.border(
                                                1.5.dp,
                                                AmberPrimary,
                                                RoundedCornerShape(3.dp)
                                            ) else Modifier
                                        )
                                        .clickable { selectedDay = day }
                                )
                            }
                        }
                    }
                }
            }

            // Selected day info pill
            Spacer(modifier = Modifier.height(12.dp))
            val currentDay = selectedDay ?: days.lastOrNull()
            if (currentDay != null) {
                val formattedDate = dateFormat.format(Date(currentDay.dateMillis))
                val desc = if (currentDay.chaptersCount > 0 || currentDay.readingMinutes > 0) {
                    "${currentDay.chaptersCount} chapters read • ${currentDay.readingMinutes} min session"
                } else {
                    "No logged reading"
                }
                Text(
                    text = "$formattedDate: $desc",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

private fun heatmapColorForLevel(level: Int): Color {
    return when (level) {
        0 -> Color(0xFF13181C)
        1 -> ChapterReadGreen.copy(alpha = 0.30f)
        2 -> ChapterReadGreen.copy(alpha = 0.55f)
        3 -> ChapterReadGreen.copy(alpha = 0.80f)
        4 -> ChapterReadGreen
        else -> Color(0xFF13181C)
    }
}
