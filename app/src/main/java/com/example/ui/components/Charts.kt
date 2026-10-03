package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BookFormat
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChapterReadGreen
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

private val ChartPalette = listOf(
    AmberPrimary,          // Gold / Amber
    LavenderAccent,        // Lavender / Blue
    ChapterReadGreen,      // Emerald
    Color(0xFF38BDF8),     // Cyan
    Color(0xFFF43F5E),     // Rose
    Color(0xFFA855F7),     // Purple
    Color(0xFFFB923C)      // Coral Orange
)

@Composable
fun GenreDonutChart(
    genreDistribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val total = genreDistribution.values.sum().coerceAtLeast(1)
    val sortedGenres = genreDistribution.entries.sortedByDescending { it.value }.take(5)

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(genreDistribution) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = SurfaceCard,
        borderColor = BorderSubtle
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Top Genres Distribution",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (genreDistribution.isEmpty()) {
                Text(
                    text = "No genre data available yet. Add books to see your breakdown.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Canvas
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            var startAngle = -90f
                            val strokeWidth = 22.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2
                            val center = Offset(size.width / 2, size.height / 2)

                            sortedGenres.forEachIndexed { index, entry ->
                                val sweep = (entry.value.toFloat() / total) * 360f * animatedProgress.value
                                val color = ChartPalette[index % ChartPalette.size]

                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = Offset(center.x - radius, center.y - radius),
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                startAngle += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$total",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Books",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legend
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sortedGenres.forEachIndexed { index, entry ->
                            val color = ChartPalette[index % ChartPalette.size]
                            val percent = (entry.value * 100) / total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = entry.key,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = TextPrimary
                                    )
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
fun FormatBreakdownChart(
    formatDistribution: Map<BookFormat, Int>,
    modifier: Modifier = Modifier
) {
    val total = formatDistribution.values.sum().coerceAtLeast(1)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = SurfaceCard,
        borderColor = BorderSubtle
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Reading Format Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            BookFormat.entries.forEach { format ->
                val count = formatDistribution[format] ?: 0
                val progress = count.toFloat() / total

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = format.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "$count books (${(progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when (format) {
                            BookFormat.PHYSICAL -> AmberPrimary
                            BookFormat.EBOOK -> LavenderAccent
                            BookFormat.AUDIOBOOK -> ChapterReadGreen
                        },
                        trackColor = Color(0xFF1B232A)
                    )
                }
            }
        }
    }
}
