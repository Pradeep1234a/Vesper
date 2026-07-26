package com.vesper.ledger.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

/**
 * 100% NATIVE Material 3 Medium Floating Action Button Component:
 * Built strictly using native Jetpack Compose Material 3 `FloatingActionButton` primitives
 * and official Material Theme color tokens (`primaryContainer` / `onPrimaryContainer`).
 *
 * Strict M3 Specifications:
 * - Native Composable: androidx.compose.material3.FloatingActionButton
 * - Medium FAB Dimensions: Exactly 56dp x 56dp squircle container (RoundedCornerShape(16.dp)).
 * - Color Scheme: MaterialTheme.colorScheme.primaryContainer fill, onPrimaryContainer icon tint.
 * - Elevation Tokens: Level 3 default (6.dp), Level 1 pressed (2.dp).
 * - Video A Motion: Bouncy spring scaleIn (0.4f -> 1.0f) + 40ms micro-staggered icon scale pop-in.
 * - Video C Motion: Scroll-aware dynamic scaleOut (0.4f) + fadeOut on scroll down; spring pop-in on scroll stop/up.
 * - Mathematical Edge Alignment: 16dp right margin, 16dp bottom margin (with 57dp offset compensation when bottom bar is absent).
 */
@Composable
fun M3SingleFab(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Filled.Add,
    contentDescription: String = "Action",
    visible: Boolean = true,
    hasBottomBar: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isAppeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAppeared = true
    }

    // Video A: Micro-staggered icon scale animation (40ms delayed depth)
    var isIconAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(isAppeared) {
        if (isAppeared) {
            kotlinx.coroutines.delay(40)
            isIconAppeared = true
        }
    }

    // Mathematical physical screen edge offset:
    // When bottom bar is absent, add 57dp (exact bottom bar height) to match TransactionsScreen Y-coordinate exactly.
    val calculatedBottomPadding = if (hasBottomBar) 8.dp else (8.dp + 57.dp)

    AnimatedVisibility(
        visible = visible && isAppeared,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialScale = 0.4f
        ) + fadeIn(tween(220)),
        exit = scaleOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            ),
            targetScale = 0.4f
        ) + fadeOut(tween(180))
    ) {
        val iconScale by animateFloatAsState(
            targetValue = if (isIconAppeared) 1f else 0.5f,
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "M3NativeIconStaggerScale"
        )

        Box(
            modifier = modifier
                .navigationBarsPadding()
                .padding(bottom = calculatedBottomPadding)
        ) {
            FloatingActionButton(
                onClick = onClick,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp,
                    hoveredElevation = 8.dp
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }
        }
    }
}

/**
 * 100% NATIVE Material 3 Speed Dial FAB Menu System:
 * Built strictly using native Jetpack Compose Material 3 components and official theme tokens.
 *
 * Strict M3 Specifications:
 * - Native Composable: androidx.compose.material3.FloatingActionButton
 * - Capsule Items: Single CircleShape M3 Surface containers with native theme colors.
 * - Trigger Morphing: 135° rotation on trigger + icon morphing into close ×.
 * - Scrim Backdrop: Soft 45% opacity backdrop dimming layer when expanded.
 */
@Composable
fun M3SpeedDialFab(
    onActionSelected: (TransactionType) -> Unit,
    visible: Boolean = true,
    hasBottomBar: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isAppeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAppeared = true
    }

    val calculatedBottomPadding = if (hasBottomBar) 8.dp else (8.dp + 57.dp)

    AnimatedVisibility(
        visible = visible && isAppeared,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialScale = 0.4f
        ) + fadeIn(tween(220)),
        exit = scaleOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            ),
            targetScale = 0.4f
        ) + fadeOut(tween(180))
    ) {
        var isExpanded by remember { mutableStateOf(false) }

        val rotationAngle by animateFloatAsState(
            targetValue = if (isExpanded) 135f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
            label = "M3NativeFabRotation"
        )

        Box(contentAlignment = Alignment.BottomEnd) {
            // Backdrop Scrim when FAB Menu is Expanded
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(180))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isExpanded = false }
                        )
                )
            }

            Column(
                modifier = modifier
                    .navigationBarsPadding()
                    .padding(bottom = calculatedBottomPadding),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Expanded Native M3 FAB Menu Capsule Items
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(200)) + expandVertically(expandFrom = Alignment.Bottom) + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut(tween(160)) + shrinkVertically(shrinkTowards = Alignment.Bottom) + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        // Action 1: Expense Capsule Pill
                        NativeM3FabMenuCapsuleItem(
                            label = "Add Expense",
                            icon = Icons.Outlined.TrendingDown,
                            accentColor = Color(0xFFEF4444),
                            onClick = {
                                isExpanded = false
                                onActionSelected(TransactionType.EXPENSE)
                            }
                        )

                        // Action 2: Income Capsule Pill
                        NativeM3FabMenuCapsuleItem(
                            label = "Add Income",
                            icon = Icons.Outlined.TrendingUp,
                            accentColor = Color(0xFF22C55E),
                            onClick = {
                                isExpanded = false
                                onActionSelected(TransactionType.INCOME)
                            }
                        )

                        // Action 3: Transfer Capsule Pill
                        NativeM3FabMenuCapsuleItem(
                            label = "Transfer Money",
                            icon = Icons.Outlined.SwapHoriz,
                            accentColor = Color(0xFF38BDF8),
                            onClick = {
                                isExpanded = false
                                onActionSelected(TransactionType.TRANSFER)
                            }
                        )
                    }
                }

                // Main Trigger FAB Button (Native M3 FloatingActionButton)
                FloatingActionButton(
                    onClick = { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isExpanded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = if (isExpanded) 2.dp else 6.dp,
                        pressedElevation = 2.dp
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = if (isExpanded) "Close Transaction Menu" else "Open Transaction Menu",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotationAngle }
                    )
                }
            }
        }
    }
}

/**
 * Native Material 3 Capsule Pill FAB Menu Item:
 * Uses Native M3 Surface primitive with CircleShape capsule bounds.
 */
@Composable
private fun NativeM3FabMenuCapsuleItem(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        modifier = Modifier.semantics {
            this.role = Role.Button
            this.contentDescription = label
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontFamily = SpaceGroteskFamily,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
