package com.vesper.ledger.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InAppNotificationBannerOverlay(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit = {}
) {
    val notificationFlow = com.vesper.ledger.data.notification.InAppNotificationController.notificationFlow
    var currentNotification by remember { mutableStateOf<com.vesper.ledger.data.model.NotificationHistory?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Physics based drag offsets
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    
    // Expand/Collapse state
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        notificationFlow.collect { notification ->
            currentNotification = notification
        }
    }

    // Reset offsets and expand state when a new notification is posted
    LaunchedEffect(currentNotification) {
        if (currentNotification != null) {
            offsetX.snapTo(0f)
            offsetY.snapTo(0f)
            isExpanded = false
            
            // Auto-dismiss after 8 seconds of idle time if not expanded
            launch {
                delay(8000L)
                if (!isExpanded) {
                    currentNotification = null
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = currentNotification != null,
            enter = slideInVertically(initialOffsetY = { -it * 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it * 2 }) + fadeOut(),
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            currentNotification?.let { item ->
                // Map category tag to clean, theme-supported Shadcn-aligned Material Vector Icons
                val categoryIcon = when (item.category) {
                    "WELCOME" -> Icons.Outlined.Info
                    "DAILY_REMINDER", "FRIENDLY_REMINDER" -> Icons.Outlined.Edit
                    "MOTIVATION" -> Icons.Outlined.Lightbulb
                    "STREAK_CELEBRATION", "ACHIEVEMENT" -> Icons.Outlined.Star
                    "WEEKLY_SUMMARY", "MONTHLY_INSIGHT" -> Icons.Outlined.TrendingUp
                    "SMART_SUGGESTIONS" -> Icons.Outlined.TipsAndUpdates
                    "WARNING" -> Icons.Outlined.Warning
                    "BACKUP_REMINDER" -> Icons.Outlined.CloudUpload
                    "PRODUCT_UPDATES" -> Icons.Outlined.NewReleases
                    else -> Icons.Outlined.Notifications
                }

                // Premium card container conforming to shapes.medium
                ShCard(
                    borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { androidx.compose.ui.unit.IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
                        .pointerInput(isExpanded) {
                            // Gesture movement: Physics-driven swipe motions
                            detectDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        if (offsetY.value > 80f && !isExpanded) {
                                            // Swipe down to Expand
                                            isExpanded = true
                                            launch { offsetY.animateTo(0f, spring()) }
                                        } else if (offsetY.value < -80f) {
                                            if (isExpanded) {
                                                // Swipe up to Compress/Collapse
                                                isExpanded = false
                                                launch { offsetY.animateTo(0f, spring()) }
                                            } else {
                                                // Swipe up to Dismiss
                                                currentNotification = null
                                            }
                                        } else if (offsetX.value > 200f || offsetX.value < -200f) {
                                            // Swipe left/right to Dismiss
                                            currentNotification = null
                                        } else {
                                            // Spring back to center
                                            launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                                            launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        offsetX.snapTo(offsetX.value + dragAmount.x)
                                        // Allow upward swipe, and allow downward swipe ONLY if not expanded
                                        if (isExpanded) {
                                            if (offsetY.value + dragAmount.y <= 0f) {
                                                offsetY.snapTo(offsetY.value + dragAmount.y)
                                            }
                                        } else {
                                            offsetY.snapTo(offsetY.value + dragAmount.y)
                                        }
                                    }
                                }
                            )
                        }
                        .clickable { isExpanded = !isExpanded } // Toggle on tap
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Vector Icon Container (linked to system accent theme)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = item.category,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Content Details
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.title,
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Expand / Compress Arrow Indicator Button
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                            contentDescription = if (isExpanded) "Compress" else "Expand",
                                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { isExpanded = !isExpanded }
                                        )

                                        // Close Action
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Close",
                                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { currentNotification = null }
                                        )
                                    }
                                }

                                Text(
                                    text = item.message,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp,
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Contextual Action Buttons shown ONLY when card is Expanded
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (item.category) {
                                    "DAILY_REMINDER", "FRIENDLY_REMINDER" -> {
                                        TextButton(
                                            onClick = {
                                                currentNotification = null
                                                onNavigate("add_transaction")
                                            }
                                        ) {
                                            Text(
                                                text = "Record Expense",
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        TextButton(
                                            onClick = {
                                                currentNotification = null
                                            }
                                        ) {
                                            Text(
                                                text = "Snooze",
                                                fontFamily = SpaceGroteskFamily,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    "BACKUP_REMINDER" -> {
                                        TextButton(
                                            onClick = {
                                                currentNotification = null
                                                onNavigate("settings")
                                            }
                                        ) {
                                            Text(
                                                text = "Backup Now",
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    "SMART_SUGGESTIONS", "WEEKLY_SUMMARY", "MONTHLY_INSIGHT" -> {
                                        TextButton(
                                            onClick = {
                                                currentNotification = null
                                                onNavigate("reports")
                                            }
                                        ) {
                                            Text(
                                                text = "View Analytics",
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
