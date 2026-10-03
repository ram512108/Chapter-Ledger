package com.example.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

data class SampleIsbn(
    val title: String,
    val isbn: String
)

@Composable
fun BarcodeScanDialog(
    onDismiss: () -> Unit,
    onIsbnSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var inputIsbn by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sampleIsbns = remember {
        listOf(
            SampleIsbn("Dune", "9780441172719"),
            SampleIsbn("Project Hail Mary", "9780593135204"),
            SampleIsbn("1984", "9780451524935"),
            SampleIsbn("Atomic Habits", "9780735211292"),
            SampleIsbn("The Hobbit", "9780547928227")
        )
    }

    // Futuristic scanning beam animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 580.dp)
                .padding(vertical = 12.dp)
                .testTag("barcode_scan_dialog"),
            colors = CardDefaults.cardColors(containerColor = BackgroundSecondary),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BorderMedium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = LavenderAccent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, LavenderAccent.copy(alpha = 0.3f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = LavenderAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ISBN Barcode Scanner",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Futuristic Viewfinder Frame with animated laser
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceElevated)
                        .border(BorderStroke(1.dp, BorderMedium), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Corner accents
                    Text(
                        text = "ALIGN ISBN BARCODE WITHIN FRAME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.2.sp
                        ),
                        color = TextTertiary.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
                    )

                    // Laser line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(2.dp)
                            .offset(y = laserOffset.dp - 20.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        LavenderAccent,
                                        AmberPrimary,
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Target indicator
                    Box(
                        modifier = Modifier
                            .size(70.dp, 40.dp)
                            .border(BorderStroke(1.5.dp, AmberPrimary.copy(alpha = 0.6f)), RoundedCornerShape(6.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input field for manual ISBN / Barcode digits
                Text(
                    text = "ENTER OR VERIFY ISBN-10 / ISBN-13",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = inputIsbn,
                    onValueChange = {
                        inputIsbn = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text("e.g., 9780141439518", color = TextTertiary, fontSize = 13.5.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("barcode_dialog_input"),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                val clean = clip.filter { it.isDigit() || it.equals('X', ignoreCase = true) }
                                if (clean.isNotBlank()) {
                                    inputIsbn = clean
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste from clipboard",
                                tint = LavenderAccent,
                                modifier = Modifier.size(18.dp)
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

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFF43F5E),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preset test barcodes
                Text(
                    text = "TEST PRESET BARCODES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = TextTertiary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleIsbns.take(3).forEach { sample ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    inputIsbn = sample.isbn
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceElevated,
                            border = BorderStroke(0.5.dp, BorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = sample.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = sample.isbn.takeLast(4),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    FuturisticPrimaryButton(
                        text = "Look Up Book",
                        onClick = {
                            val clean = inputIsbn.filter { it.isDigit() || it.equals('X', ignoreCase = true) }
                            if (clean.length in 9..14) {
                                onIsbnSelected(clean)
                                onDismiss()
                            } else {
                                errorMessage = "Please enter a valid 10 or 13-digit ISBN barcode"
                            }
                        },
                        icon = Icons.Default.Search,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("lookup_isbn_button")
                    )
                }
            }
        }
    }
}
