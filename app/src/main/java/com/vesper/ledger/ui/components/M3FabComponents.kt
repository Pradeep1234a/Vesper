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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

/**
 * 3D Elevated Single Action Floating Action Button:
 * Physical 3D tactile push-button geometry with top specular lighting gradient, dual ambient/spot glow,
 * exact 8dp bottom gap & 16dp card edge alignment.
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
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "3DFabPressScale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isPressed) 3f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "3DFabTranslation"
    )

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp, end = 16.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY
            }
            .shadow(
                elevation = if (isPressed) 4.dp else 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black,
                spotColor = Color(0xFF38BDF8).copy(alpha = if (isPressed) 0.3f else 0.7f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E2E35),
                        Color(0xFF141417)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF38BDF8),
                        Color(0xFF0284C7)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner 3D Specular Ring Reflection Highlight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
        )
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * 3D Elevated Multi-Action Speed Dial FAB Menu:
 * Tactical 3D physical push-button trigger with expandable 3D floating action item stack.
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
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "Main3DFabScale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isPressed) 3f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "Main3DFabTranslation"
    )

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "3DFabRotation"
    )

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp, end = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Expanded 3D FAB Menu Floating Action Stack
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
                M33DFabMenuItem(
                    label = "Add Expense",
                    icon = Icons.Outlined.TrendingDown,
                    badgeColor = Color(0xFFEF4444),
                    onClick = {
                        isExpanded = false
                        onActionSelected(TransactionType.EXPENSE)
                    }
                )

                // Action 2: Income (+ Income)
                M33DFabMenuItem(
                    label = "Add Income",
                    icon = Icons.Outlined.TrendingUp,
                    badgeColor = Color(0xFF22C55E),
                    onClick = {
                        isExpanded = false
                        onActionSelected(TransactionType.INCOME)
                    }
                )

                // Action 3: Transfer (⇄ Transfer)
                M33DFabMenuItem(
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

        // Main 3D Elevated Trigger FAB Button
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = fabScale
                    scaleY = fabScale
                    this.translationY = translationY
                }
                .shadow(
                    elevation = if (isPressed) 4.dp else 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black,
                    spotColor = Color(0xFF38BDF8).copy(alpha = if (isPressed) 0.3f else 0.7f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isExpanded) {
                            listOf(Color(0xFF35353F), Color(0xFF1F1F24))
                        } else {
                            listOf(Color(0xFF2E2E35), Color(0xFF141417))
                        }
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF38BDF8),
                            Color(0xFF0284C7)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(interactionSource = interactionSource, indication = null) { isExpanded = !isExpanded }
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // Inner 3D Reflection Frame
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            )
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
private fun M33DFabMenuItem(
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
        label = "3DItemPressScale"
    )

    val itemTranslationY by animateFloatAsState(
        targetValue = if (isPressed) 2f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "3DItemTranslation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .graphicsLayer {
                scaleX = itemScale
                scaleY = itemScale
                this.translationY = itemTranslationY
            }
            .clickable(interactionSource = itemInteractionSource, indication = null) { onClick() }
    ) {
        // 3D Elevated Label Badge Box
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(8.dp), ambientColor = Color.Black)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2C2C33), Color(0xFF1E1E23))
                    )
                )
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

        // 3D Elevated Mini FAB Container (48dp x 48dp)
        Box(
            modifier = Modifier
                .shadow(8.dp, CircleShape, ambientColor = Color.Black, spotColor = badgeColor.copy(alpha = 0.5f))
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E2E35), Color(0xFF141417))
                    )
                )
                .border(1.5.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            )
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = badgeColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
