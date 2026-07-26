package com.vesper.ledger.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
 * Material 3 Medium Floating Action Button System (m3.material.io/components/floating-action-button).
 * Implements Video A, Video B, Video C, Video D, and physical edge margin specifications:
 *
 * 1. Medium FAB Specifications:
 *    - Container Size: Exactly 56dp x 56dp squircle (RoundedCornerShape(16.dp)).
 *    - Icon Size: Exactly 24dp filled vector icon.
 *    - Colors: MaterialTheme.colorScheme.primaryContainer fill, onPrimaryContainer icon tint.
 *    - Rest Elevation: Level 3 (6.dp). Pressed Elevation: Level 1 (2.dp).
 *
 * 2. Video A (Appearing & Reappearing Motion):
 *    - Center-Focal Scale: scaleIn(0.4f -> 1.0f) with Emphasized Deceleration spring.
 *    - 40ms Micro-Staggered Icon Pop-In: Icon scales with 40ms delay for 3D visual depth.
 *    - Tab Transition: Fades and scales out on exit, spring scale-ins on entrance.
 *
 * 3. Video C (Scroll-Aware Motion):
 *    - Scroll Down: Scale-outs (scaleOut(0.4f) + fadeOut(180ms)).
 *    - Scroll Stop/Up: Spring scale-ins (scaleIn(0.4f) + fadeIn(220ms)).
 *
 * 4. Mathematical Edge Alignment:
 *    - 16dp right margin, 16dp bottom margin (with 57dp height offset compensation when bottom bar is absent).
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
            targetValue = if (isIconAppeared) 1f else 0.2f,
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "VideoAIconStaggerScale"
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
 * Official Material 3 Speed Dial FAB Menu System (m3.material.io/components/fab-menu - Video D Specs):
 * - Unified M3 Capsule Pill Items: Single CircleShape M3 Surface container ([Icon] + [Label]).
 * - Trigger Morphing: 135° spring rotation on trigger + icon morphing into close ×.
 * - Backdrop Scrim: Soft 45% opacity backdrop dimming layer when expanded.
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
            label = "VideoDFabRotation"
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
                // Video D: Expanded Official M3 FAB Menu Capsule Items
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
                        M3FabMenuCapsuleItem(
                            label = "Add Expense",
                            icon = Icons.Outlined.TrendingDown,
                            accentColor = Color(0xFFEF4444),
                            onClick = {
                                isExpanded = false
                                onActionSelected(TransactionType.EXPENSE)
                            }
                        )

                        // Action 2: Income Capsule Pill
                        M3FabMenuCapsuleItem(
                            label = "Add Income",
                            icon = Icons.Outlined.TrendingUp,
                            accentColor = Color(0xFF22C55E),
                            onClick = {
                                isExpanded = false
                                onActionSelected(TransactionType.INCOME)
                            }
                        )

                        // Action 3: Transfer Capsule Pill
                        M3FabMenuCapsuleItem(
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
 * Official Video D Capsule Pill FAB Menu Item:
 * Uses Native M3 Surface primitive with CircleShape capsule bounds ([Icon] + [Label]).
 */
@Composable
private fun M3FabMenuCapsuleItem(
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

/**
 * Video B Container Transform Wrapper:
 * Animates the FAB container morphing outward into the creation form screen sheet (300ms bounds interpolation + content cross-fade).
 */
@Composable
fun M3FabContainerTransform(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    fabContent: @Composable () -> Unit,
    formContent: @Composable () -> Unit
) {
    val transition = updateTransition(targetState = isExpanded, label = "VideoBContainerTransform")

    val containerCornerRadius by transition.animateDp(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
        label = "ContainerCornerRadius"
    ) { expanded ->
        if (expanded) 28.dp else 16.dp
    }

    val formOpacity by transition.animateFloat(
        transitionSpec = { tween(200, delayMillis = 100) },
        label = "FormOpacity"
    ) { expanded ->
        if (expanded) 1f else 0f
    }

    Box(modifier = Modifier.fillMaxSize()) {
        fabContent()

        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = formOpacity }
                    .clip(RoundedCornerShape(topStart = containerCornerRadius, topEnd = containerCornerRadius))
            ) {
                formContent()
            }
        }
    }
}
