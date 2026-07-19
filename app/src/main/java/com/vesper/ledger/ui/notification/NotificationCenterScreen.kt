package com.vesper.ledger.ui.notification

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vesper.ledger.data.model.NotificationHistory
import com.vesper.ledger.data.notification.NotificationCategory
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.SectionHeader
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.ShTextField
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationCenterScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    // Trigger simulated push sync check on launch to retrieve any background/simulated alerts
    LaunchedEffect(Unit) {
        viewModel.triggerSync()
    }

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Notifications",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { viewModel.markAllAsRead() },
                        enabled = notifications.any { !it.isRead }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark all read",
                            tint = if (notifications.any { !it.isRead }) 
                                MaterialTheme.colorScheme.onBackground 
                            else 
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.clearAll() },
                        enabled = notifications.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear all notifications",
                            tint = if (notifications.isNotEmpty()) 
                                MaterialTheme.colorScheme.onBackground 
                            else 
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            ShTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                label = "Search Notifications",
                placeholder = "Type to search by title or message...",
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Category Filters LazyRow
            val filterOptions = listOf(
                null to "All",
                NotificationCategory.DAILY_REMINDER.name to "Reminders",
                NotificationCategory.SMART_SUGGESTIONS.name to "Suggestions",
                NotificationCategory.WEEKLY_SUMMARY.name to "Insights",
                NotificationCategory.ACHIEVEMENT.name to "Goals",
                NotificationCategory.BACKUP_REMINDER.name to "Security",
                NotificationCategory.PRODUCT_UPDATES.name to "Updates"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                items(filterOptions) { (catId, label) ->
                    val isSelected = selectedCategory == catId
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { viewModel.selectedCategory.value = catId }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            if (notifications.isEmpty()) {
                // Empty state view
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "No notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "All caught up",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No new notifications found in this view.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Timeline grouped lists
                val grouped = remember(notifications) {
                    notifications.groupBy { getTimelineGroup(it.timestamp) }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp)
                ) {
                    // Show timeline sections
                    val sections = listOf("Today", "Yesterday", "This Week", "Earlier")
                    
                    sections.forEach { section ->
                        val sectionItems = grouped[section] ?: emptyList()
                        if (sectionItems.isNotEmpty()) {
                            stickyHeader {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    SectionHeader(
                                        title = section,
                                        modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            items(sectionItems, key = { it.id }) { item ->
                                NotificationCardItem(
                                    item = item,
                                    onClick = {
                                        viewModel.markAsRead(item.id)
                                        viewModel.trackClicked(item.id)
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteNotification(item.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCardItem(
    item: NotificationHistory,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeString = formatter.format(Date(item.timestamp))

    val borderStroke = if (!item.isRead) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    }

    ShCard(
        borderStroke = borderStroke,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Category Indicator Dot / Icon Area
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.CenterVertically)
                    .clip(CircleShape)
                    .background(
                        if (item.isRead) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayLabel = when (item.category) {
                        "WELCOME" -> "Welcome"
                        "DAILY_REMINDER", "FRIENDLY_REMINDER" -> "Reminder"
                        "MOTIVATION" -> "Motivation"
                        "STREAK_CELEBRATION", "ACHIEVEMENT" -> "Achievement"
                        "WEEKLY_SUMMARY", "MONTHLY_INSIGHT" -> "Insight"
                        "SMART_SUGGESTIONS" -> "Suggestion"
                        "WARNING" -> "Alert"
                        "BACKUP_REMINDER" -> "Security"
                        "PRODUCT_UPDATES" -> "Update"
                        else -> item.category.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() }
                    }
                    Text(
                        text = displayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete Icon
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete notification",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDeleteClick() }
            )
        }
    }
}

private fun getTimelineGroup(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    val now = Calendar.getInstance()

    calendar.timeInMillis = timestamp

    val diffMs = now.timeInMillis - timestamp
    val diffDays = diffMs / (24 * 60 * 60 * 1000L)

    val currentDay = now.get(Calendar.DAY_OF_YEAR)
    val itemDay = calendar.get(Calendar.DAY_OF_YEAR)
    val currentYear = now.get(Calendar.YEAR)
    val itemYear = calendar.get(Calendar.YEAR)

    return when {
        currentYear == itemYear && currentDay == itemDay -> "Today"
        currentYear == itemYear && currentDay - itemDay == 1 -> "Yesterday"
        diffDays < 7 -> "This Week"
        else -> "Earlier"
    }
}
