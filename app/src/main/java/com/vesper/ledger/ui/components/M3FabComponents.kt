package com.vesper.ledger.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

/**
 * Official Material 3 Single Action Floating Action Button:
 * Standard 56dp M3 container, 16dp squircle shape, 6dp tonal elevation shadow.
 * Formatted with 8dp bottom gap and EXACT card-aligned 16dp edge margin without duplicate offset.
 */
@Composable
fun M3SingleFab(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    contentDescription: String = "Action",
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "M3FabPressScale"
    )

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp) // Scaffold already applies 16dp end padding; matching transaction card margin perfectly
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 3.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black,
                spotColor = Color(0xFF38BDF8).copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF38BDF8))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFF09090B),
            modifier = Modifier.size(26.dp)
        )
    }
}

/**
 * Official Material 3 FAB Menu (Speed Dial / Floating Action Menu):
 * Complies strictly with Material 3 FAB Menu guidelines (m3.material.io/components/fab-menu/overview).
 * 8dp bottom gap, exact card edge alignment, smooth spring motion and press response.
 */
@Composable
fun M3SpeedDialFab(
    onActionSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val fabScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "MainM3FabScale"
    )

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "M3FabRotation"
    )

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp), // Scaffold already applies 16dp end padding; aligns with transaction card right edge
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expanded Material 3 FAB Menu Floating Stack
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(200)) + expandVertically(expandFrom = Alignment.Bottom) + slideInVertically(initialOffsetY = { it / 3 }),
            exit = fadeOut(tween(160)) + shrinkVertically(shrinkTowards = Alignment.Bottom) + slideOutVertically(targetOffsetY = { it / 3 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                // Action 1: Expense (- Expense)
                M3FabMenuItem(
                    label = "Add Expense",
                    icon = Icons.Outlined.TrendingDown,
                    badgeColor = Color(0xFFEF4444),
                    onClick = {
                        isExpanded = false
                        onActionSelected(TransactionType.EXPENSE)
                    }
                )

                // Action 2: Income (+ Income)
                M3FabMenuItem(
                    label = "Add Income",
                    icon = Icons.Outlined.TrendingUp,
                    badgeColor = Color(0xFF22C55E),
                    onClick = {
                        isExpanded = false
                        onActionSelected(TransactionType.INCOME)
                    }
                )

                // Action 3: Transfer (⇄ Transfer)
                M3FabMenuItem(
                    label = "Transfer Money",
                    icon = Icons.Outlined.SwapHoriz,
                    badgeColor = Color(0xFF38BDF8),
                    onClick = {
                        isExpanded = false
                        onActionSelected(TransactionType.TRANSFER)
                    }
                )
            }
        }

        // Main Trigger FAB Button (Material 3 Cyan Container with Smooth 135° Rotation)
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = fabScale
                    scaleY = fabScale
                }
                .shadow(
                    elevation = if (isPressed) 3.dp else 6.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black,
                    spotColor = Color(0xFF38BDF8).copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(if (isExpanded) Color(0xFF18181B) else Color(0xFF38BDF8))
                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                .clickable(interactionSource = interactionSource, indication = null) { isExpanded = !isExpanded }
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) "Close FAB Menu" else "Expand FAB Menu",
                tint = if (isExpanded) Color.White else Color(0xFF09090B),
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { rotationZ = rotationAngle }
            )
        }
    }
}

@Composable
private fun M3FabMenuItem(
    label: String,
    icon: ImageVector,
    badgeColor: Color,
    onClick: () -> Unit
) {
    val itemInteractionSource = remember { MutableInteractionSource() }
    val isPressed by itemInteractionSource.collectIsPressedAsState()

    val itemScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "M3ItemPressScale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .graphicsLayer {
                scaleX = itemScale
                scaleY = itemScale
            }
            .clickable(interactionSource = itemInteractionSource, indication = null) { onClick() }
    ) {
        // M3 Elevated Label Badge Box
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(8.dp), ambientColor = Color.Black)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF18181B))
                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                fontFamily = SpaceGroteskFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // M3 Mini FAB Container (48dp x 48dp)
        Box(
            modifier = Modifier
                .shadow(6.dp, CircleShape, ambientColor = Color.Black, spotColor = badgeColor.copy(alpha = 0.4f))
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF18181B))
                .border(1.5.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = badgeColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
