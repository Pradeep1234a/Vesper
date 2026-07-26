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
 * Standard Material 3 Single Action Floating Action Button:
 * 56dp height, 16dp squircle radius, monochrome elevated surface, exact 8dp bottom gap & 16dp card-aligned margin.
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
        label = "FabPressScale"
    )

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp, end = 16.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black, spotColor = Color(0xFF38BDF8).copy(alpha = 0.6f))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF18181B))
            .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Material 3 FAB Menu (Speed Dial / Floating Action Menu):
 * Multi-action expanded FAB menu according to M3 FAB Menu specification (m3.material.io/components/fab-menu/overview).
 * 8dp exact bottom gap, monochrome container colors, smooth spring motion and pressed response effects.
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
        label = "MainFabScale"
    )

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "FabRotation"
    )

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp, end = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Expanded M3 FAB Menu Floating Stack
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(220)) + expandVertically(expandFrom = Alignment.Bottom) + slideInVertically(initialOffsetY = { it / 3 }),
            exit = fadeOut(tween(180)) + shrinkVertically(shrinkTowards = Alignment.Bottom) + slideOutVertically(targetOffsetY = { it / 3 })
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

        // Main Trigger FAB Button (Monochrome Surface with Cyan Border & Rotation Effect)
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = fabScale
                    scaleY = fabScale
                }
                .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black, spotColor = Color(0xFF38BDF8).copy(alpha = 0.6f))
                .clip(RoundedCornerShape(16.dp))
                .background(if (isExpanded) Color(0xFF242429) else Color(0xFF18181B))
                .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                .clickable(interactionSource = interactionSource, indication = null) { isExpanded = !isExpanded }
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) "Close FAB Menu" else "Expand FAB Menu",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
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
        label = "ItemPressScale"
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
        // M3 Elevated Label Pill Box
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(8.dp), ambientColor = Color.Black)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF242429))
                .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(8.dp))
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

        // Mini FAB Container (48dp x 48dp)
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
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
