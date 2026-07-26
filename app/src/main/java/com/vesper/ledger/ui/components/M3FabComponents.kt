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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

/**
 * Material 3 Extended Floating Action Button with M3 Motion (Video A, C & D Specs):
 * - Video A: Center-focal scaleIn (0.4f -> 1.0f) + 40ms micro-staggered icon pop-in.
 * - Video C: Scroll-aware collapse/hide on list scroll down; spring pop-in on scroll stop/up.
 * - Video D: Collapses label text smoothly into a 56dp icon FAB on scroll, and expands label when at top!
 * - Mathematical Edge Alignment: Evaluates physical screen edge margin to match TransactionsScreen Y-coordinate exactly.
 */
@Composable
fun M3SingleFab(
    onClick: () -> Unit,
    label: String = "",
    icon: ImageVector = Icons.Filled.Add,
    contentDescription: String = "Action",
    visible: Boolean = true,
    isExpanded: Boolean = false,
    hasBottomBar: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isAppeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAppeared = true
    }

    // Micro-staggered icon scale animation (40ms delayed depth from Video A)
    var isIconAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(isAppeared) {
        if (isAppeared) {
            kotlinx.coroutines.delay(40)
            isIconAppeared = true
        }
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
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val pressScale by animateFloatAsState(
            targetValue = if (isPressed) 0.90f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "M3FabPressScale"
        )

        val currentElevation by animateDpAsState(
            targetValue = if (isPressed) 2.dp else 6.dp,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "M3FabElevation"
        )

        val iconScale by animateFloatAsState(
            targetValue = if (isIconAppeared) 1f else 0.5f,
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "M3IconStaggerScale"
        )

        Box(
            modifier = modifier
                .navigationBarsPadding()
                .padding(bottom = calculatedBottomPadding)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .shadow(
                    elevation = currentElevation,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black,
                    spotColor = Color(0xFF38BDF8).copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF50C7FB),
                            Color(0xFF0EA5E9)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClickLabel = contentDescription,
                    onClick = onClick
                )
                .semantics {
                    this.role = Role.Button
                    this.contentDescription = contentDescription
                }
                .height(56.dp)
                .animateContentSize(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioLowBouncy
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = if (isExpanded && label.isNotBlank()) 20.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color(0xFF09090B),
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )

                if (isExpanded && label.isNotBlank()) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = label,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF09090B)
                    )
                }
            }
        }
    }
}

/**
 * Official Material 3 Speed Dial FAB Menu with Motion (Video A, C & D Specs):
 * - Video A: Center-focal scaleIn + 40ms micro-stagger icon pop-in.
 * - Video C: Scroll-aware collapse/hide on list scroll down; spring pop-in on scroll stop/up.
 * - Video D: Speed Dial Transformation with 135° rotation on trigger FAB + staggered mini FAB stack.
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
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val fabScale by animateFloatAsState(
            targetValue = if (isPressed) 0.90f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "MainM3FabScale"
        )

        val rotationAngle by animateFloatAsState(
            targetValue = if (isExpanded) 135f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
            label = "M3FabRotation"
        )

        val currentElevation by animateDpAsState(
            targetValue = if (isExpanded || isPressed) 2.dp else 6.dp,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "M3FabElevation"
        )

        Column(
            modifier = modifier
                .navigationBarsPadding()
                .padding(bottom = calculatedBottomPadding),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Expanded Speed Dial Floating Menu Stack
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

            // Main Trigger FAB Button (3D Elevated M3 Squircle Container)
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = fabScale
                        scaleY = fabScale
                    }
                    .shadow(
                        elevation = currentElevation,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black,
                        spotColor = Color(0xFF38BDF8).copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isExpanded) {
                            Brush.verticalGradient(listOf(Color(0xFF27272A), Color(0xFF18181B)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFF50C7FB), Color(0xFF0EA5E9)))
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = if (isExpanded) {
                            Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0EA5E9)))
                        } else {
                            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.05f)))
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClickLabel = if (isExpanded) "Close Transaction Menu" else "Open Transaction Menu",
                        onClick = { isExpanded = !isExpanded }
                    )
                    .semantics {
                        this.role = Role.Button
                        this.contentDescription = if (isExpanded) "Close Transaction Menu" else "Open Transaction Menu"
                    }
                    .size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (isExpanded) "Close Transaction Menu" else "Open Transaction Menu",
                    tint = if (isExpanded) Color.White else Color(0xFF09090B),
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = rotationAngle }
                )
            }
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
            .clickable(
                interactionSource = itemInteractionSource,
                indication = null,
                onClickLabel = label,
                onClick = onClick
            )
            .semantics {
                this.role = Role.Button
                this.contentDescription = label
            }
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
