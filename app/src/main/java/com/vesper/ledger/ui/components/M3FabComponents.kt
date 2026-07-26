package com.vesper.ledger.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CompareArrows
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
 * Standard Material 3 Single Action Floating Action Button aligned with M3 Guidelines:
 * 56dp height, 16dp squircle radius, 16dp spacing from edge/bottom nav.
 */
@Composable
fun M3SingleFab(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    contentDescription: String = "Action",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp, end = 16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF38BDF8).copy(alpha = 0.5f))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF38BDF8))
            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFF09090B),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Material 3 Multi-Action Speed Dial FAB:
 * Expands floating action items vertically with animated icon rotation (0 -> 45 deg) and M3 spatial rhythm.
 */
@Composable
fun M3SpeedDialFab(
    onActionSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "FabRotation"
    )

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expanded Speed Dial Options
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(180)) + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action 1: Expense
                SpeedDialItem(
                    label = "Expense",
                    icon = Icons.Outlined.TrendingDown,
                    accentColor = Color(0xFFEF4444),
                    onClick = {
                        isExpanded = false
                        onActionSelected(TransactionType.EXPENSE)
                    }
                )

                // Action 2: Income
                SpeedDialItem(
                    label = "Income",
                    icon = Icons.Outlined.TrendingUp,
                    accentColor = Color(0xFF22C55E),
                    onClick = {
                        isExpanded = false
                        onActionSelected(TransactionType.INCOME)
                    }
                )

                // Action 3: Transfer
                SpeedDialItem(
                    label = "Transfer",
                    icon = Icons.Outlined.SwapHoriz,
                    accentColor = Color(0xFF38BDF8),
                    onClick = {
                        isExpanded = false
                        onActionSelected(TransactionType.TRANSFER)
                    }
                )
            }
        }

        // Main Trigger FAB (Rotates from + to X)
        Box(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF38BDF8).copy(alpha = 0.5f))
                .clip(RoundedCornerShape(16.dp))
                .background(if (isExpanded) Color(0xFF18181B) else Color(0xFF38BDF8))
                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                .clickable { isExpanded = !isExpanded }
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) "Close Menu" else "Add Transaction",
                tint = if (isExpanded) Color.White else Color(0xFF09090B),
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { rotationZ = rotationAngle }
            )
        }
    }
}

@Composable
private fun SpeedDialItem(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        // Label Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF18181B))
                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                fontFamily = SpaceGroteskFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Mini FAB Action Icon Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.18f))
                .border(1.dp, accentColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
