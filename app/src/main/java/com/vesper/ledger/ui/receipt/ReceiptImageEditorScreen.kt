package com.vesper.ledger.ui.receipt

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

@Composable
fun ReceiptImageEditorScreen(
    imageUri: Uri,
    onRetakeClick: () -> Unit,
    onConfirmEnhancement: (Uri) -> Unit
) {
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var contrastValue by remember { mutableFloatStateOf(1.2f) }
    var brightnessValue by remember { mutableFloatStateOf(0f) }
    var isBlurWarning by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChildHeader(
                title = "Adjust & Enhance",
                onBackClick = onRetakeClick,
                actions = {
                    IconButton(onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f }) {
                        Icon(Icons.Filled.RotateRight, contentDescription = "Rotate", tint = Color.White)
                    }
                }
            )

            // Blur Detection Notice
            if (isBlurWarning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Image looks slightly blurry. Hold camera steady or retake for best OCR accuracy.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                    )
                }
            }

            // Image Preview Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Receipt Crop Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            rotationZ = rotationDegrees
                        )
                )
            }

            // Adjustment Sliders (Contrast & Brightness for Thermal Paper Text Recovery)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Contrast, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thermal Contrast Boost", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(String.format("%.1fx", contrastValue), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                Slider(
                    value = contrastValue,
                    onValueChange = { contrastValue = it },
                    valueRange = 0.8f..2.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetakeClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retake", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onConfirmEnhancement(imageUri) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Process with AI", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
