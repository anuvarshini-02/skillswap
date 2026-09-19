package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType
import com.example.ui.components.EmptyState
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillSwapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: SkillSwapViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRequests: () -> Unit,
    onNavigateToSessions: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allNotifications by viewModel.allNotifications.collectAsState()

    val userNotifications = allNotifications.filter { it.userId == currentUser?.userId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (userNotifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.markAllNotificationsRead() }) {
                            Icon(
                                imageVector = Icons.Outlined.DoneAll,
                                contentDescription = "Mark all read",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.clearAllNotifications() }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (userNotifications.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.NotificationsNone,
                    title = "No Notifications",
                    message = "You're all caught up! Updates regarding your session requests, earned points, and peer reviews will show here."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(userNotifications, key = { it.notificationId }) { notif ->
                        NotificationItem(
                            notification = notif,
                            onClick = {
                                viewModel.markNotificationAsRead(notif.notificationId)
                                when (notif.type) {
                                    NotificationType.REQUEST_RECEIVED,
                                    NotificationType.REQUEST_ACCEPTED,
                                    NotificationType.REQUEST_REJECTED -> onNavigateToRequests()
                                    NotificationType.SESSION_REMINDER,
                                    NotificationType.SESSION_COMPLETED,
                                    NotificationType.NEW_REVIEW -> onNavigateToSessions()
                                    else -> {}
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val icon = when (notification.type) {
        NotificationType.POINTS_EARNED -> Icons.Default.MonetizationOn
        NotificationType.POINTS_SPENT -> Icons.Default.Stars
        NotificationType.REQUEST_RECEIVED -> Icons.Default.Mail
        NotificationType.REQUEST_ACCEPTED -> Icons.Default.CheckCircle
        NotificationType.REQUEST_REJECTED -> Icons.Default.Cancel
        NotificationType.SESSION_REMINDER -> Icons.Default.Event
        NotificationType.SESSION_COMPLETED -> Icons.Default.TaskAlt
        NotificationType.NEW_REVIEW -> Icons.Default.Star
        NotificationType.SYSTEM_WELCOME -> Icons.Default.CardGiftcard
    }

    val iconColor = when (notification.type) {
        NotificationType.POINTS_EARNED, NotificationType.SYSTEM_WELCOME -> GoldPointsDark
        NotificationType.REQUEST_ACCEPTED, NotificationType.SESSION_COMPLETED -> SuccessGreen
        NotificationType.REQUEST_REJECTED -> ErrorRed
        else -> MaterialTheme.colorScheme.primary
    }

    val timeFormatted = remember(notification.timestamp) {
        java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(notification.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notification_item_${notification.notificationId}"),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
