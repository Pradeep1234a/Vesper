package com.vesper.ledger.ui.receipt

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vesper.ledger.data.receipt.ReceiptLineItem

@Composable
fun ReceiptInteractiveCanvas(
    imageUriStr: String,
    lineItems: List<ReceiptLineItem>,
    selectedItemId: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.8f, 4f)
        offset += offsetChange
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .transformable(state = transformState),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            AsyncImage(
                model = imageUriStr.ifBlank { "sample_receipt.png" },
                contentDescription = "Scanned Receipt Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Bounding Box Overlays
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                lineItems.forEach { item ->
                    val rect = item.boundingBox
                    if (rect != null) {
                        val isSelected = item.id == selectedItemId
                        val boxColor = if (isSelected) Color.White else if (item.isNeedsReview) Color(0xFFA1A1AA) else Color(0xFF71717A)
                        val strokeWidth = if (isSelected) 4f else 2f

                        drawRect(
                            color = boxColor.copy(alpha = 0.25f),
                            topLeft = Offset(rect.left, rect.top),
                            size = Size(rect.width, rect.height)
                        )

                        drawRect(
                            color = boxColor,
                            topLeft = Offset(rect.left, rect.top),
                            size = Size(rect.width, rect.height),
                            style = Stroke(
                                width = strokeWidth,
                                pathEffect = if (!isSelected) PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f) else null
                            )
                        )
                    }
                }
            }
        }

        // Floating Control Overlay (Zoom In, Zoom Out, Reset Center)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { scale = (scale * 1.25f).coerceAtMost(4f) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(
                onClick = { scale = (scale / 1.25f).coerceAtLeast(0.8f) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(
                onClick = { scale = 1f; offset = Offset.Zero },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Outlined.CenterFocusWeak, contentDescription = "Reset Zoom", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
