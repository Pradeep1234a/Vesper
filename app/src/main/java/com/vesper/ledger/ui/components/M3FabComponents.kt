package com.vesper.ledger.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

/**
 * Remembers scroll-aware FAB visibility based on LazyListState.
 * When scrolling starts -> returns false (FAB becomes invisible).
 * When scrolling stops or at top -> returns true (FAB pops back in).
 */
@Composable
fun rememberFabVisibility(lazyListState: LazyListState): State<Boolean> {
    return remember(lazyListState) {
        derivedStateOf {
            !lazyListState.isScrollInProgress || lazyListState.firstVisibleItemIndex == 0
        }
    }
}

/**
 * Material 3 Large Elevated Floating Action Button (m3.material.io/components/floating-action-button).
 *
 * Design & Layout Specs:
 * - Size: 96.dp x 96.dp Large FAB (RoundedCornerShape(28.dp)).
 * - Icon: 36.dp filled vector icon.
 * - Colors: Material 3 primaryContainer fill with onPrimaryContainer tint.
 * - Elevation: Level 3 Elevated Effect (8.dp rest, 3.dp pressed, 10.dp hovered).
 * - Edge Alignment:
 *   - End/Right Margin: 16.dp.
 *   - Bottom Gap WITH Bottom Bar: 16.dp above navigation bar.
 *   - Bottom Gap WITHOUT Bottom Bar: 57.dp (bottom bar height) + 16.dp = 73.dp, matching Y-coordinate seamlessly.
 */
@Composable
fun M3SingleFab(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Filled.Add,
    contentDescription: String = "Action",
    visible: Boolean = true,
    hasBottomBar: Boolean = false,
    fabSize: Dp = 96.dp,
    iconSize: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    var isAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isAppeared = true
    }

    // 40ms micro-staggered icon scale animation
    var isIconAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(isAppeared) {
        if (isAppeared) {
            kotlinx.coroutines.delay(40)
            isIconAppeared = true
        }
    }

    // Physical edge alignment margin calculation:
    // With bottom bar: 16.dp margin above bar.
    // Without bottom bar: 57.dp + 16.dp = 73.dp margin, achieving identical Y-coordinate alignment.
    val calculatedBottomPadding = if (hasBottomBar) 16.dp else (16.dp + 57.dp)

    AnimatedVisibility(
        visible = visible && isAppeared,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialScale = 0.3f
        ) + fadeIn(tween(200)),
        exit = scaleOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            ),
            targetScale = 0.3f
        ) + fadeOut(tween(150))
    ) {
        val iconScale by animateFloatAsState(
            targetValue = if (isIconAppeared) 1f else 0.2f,
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "M3IconStaggerScale"
        )

        Box(
            modifier = modifier
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = calculatedBottomPadding)
        ) {
            LargeFloatingActionButton(
                onClick = onClick,
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 3.dp,
                    hoveredElevation = 10.dp
                ),
                modifier = Modifier.size(fabSize)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .size(iconSize)
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
 * Official Material 3 Speed Dial Large FAB Menu System:
 * - Size: 96.dp Large FAB morphing speed dial button with 135° rotation on trigger '+'.
 * - Capsule Pill Menu Items: Elevated CircleShape M3 Surface items ([Icon] + [Label]).
 * - Scroll-Aware: Automatically hides on scroll start, pops in on scroll stop/up.
 * - Backdrop Scrim: 45% dark scrim overlay when open.
 */
@Composable
fun M3SpeedDialFab(
    onActionSelected: (TransactionType) -> Unit,
    visible: Boolean = true,
    hasBottomBar: Boolean = true,
    fabSize: Dp = 96.dp,
    iconSize: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    var isAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isAppeared = true
    }

    val calculatedBottomPadding = if (hasBottomBar) 16.dp else (16.dp + 57.dp)

    AnimatedVisibility(
        visible = visible && isAppeared,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialScale = 0.3f
        ) + fadeIn(tween(200)),
        exit = scaleOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            ),
            targetScale = 0.3f
        ) + fadeOut(tween(150))
    ) {
        var isExpanded by remember { mutableStateOf(false) }

        val rotationAngle by animateFloatAsState(
            targetValue = if (isExpanded) 135f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
            label = "M3SpeedDialRotation"
        )

        Box(contentAlignment = Alignment.BottomEnd) {
            // Backdrop dimming scrim when menu is open
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
                    .padding(end = 16.dp, bottom = calculatedBottomPadding),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // M3 Capsule Pill Action Items
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
                        M3FabMenuCapsuleItem(
                            label = "Add Expense",
                            icon = Icons.Outlined.TrendingDown,
                            accentColor = Color(0xFFEF4444),
                            onClick = {
                                isExpanded = false
                                onActionSelected(TransactionType.EXPENSE)
                            }
                        )

                        M3FabMenuCapsuleItem(
                            label = "Add Income",
                            icon = Icons.Outlined.TrendingUp,
                            accentColor = Color(0xFF22C55E),
                            onClick = {
                                isExpanded = false
                                onActionSelected(TransactionType.INCOME)
                            }
                        )

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

                // Main Trigger Large FAB Button
                LargeFloatingActionButton(
                    onClick = { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isExpanded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = if (isExpanded) 3.dp else 8.dp,
                        pressedElevation = 3.dp
                    ),
                    modifier = Modifier.size(fabSize)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = if (isExpanded) "Close Menu" else "Open Menu",
                        modifier = Modifier
                            .size(iconSize)
                            .graphicsLayer { rotationZ = rotationAngle }
                    )
                }
            }
        }
    }
}

/**
 * Official Material 3 Speed Dial Menu Capsule Pill Item.
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
        shadowElevation = 8.dp,
        modifier = Modifier.semantics {
            this.role = Role.Button
            this.contentDescription = label
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontFamily = SpaceGroteskFamily,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Material 3 Container Transform Expanding Morphing Animation:
 * Animates the FAB container morphing outward into the creation sheet/screen bounds (300ms spring bounds interpolation + content cross-fade).
 */
@Composable
fun M3FabContainerTransform(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    fabContent: @Composable () -> Unit,
    formContent: @Composable () -> Unit
) {
    val transition = updateTransition(targetState = isExpanded, label = "M3ContainerTransform")

    val containerCornerRadius by transition.animateDp(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
        label = "ContainerCornerRadius"
    ) { expanded ->
        if (expanded) 28.dp else 28.dp
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
