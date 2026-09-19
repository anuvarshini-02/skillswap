package com.example.data.model

enum class NotificationType {
    REQUEST_RECEIVED,
    REQUEST_ACCEPTED,
    REQUEST_REJECTED,
    SESSION_REMINDER,
    SESSION_COMPLETED,
    POINTS_EARNED,
    POINTS_SPENT,
    NEW_REVIEW,
    SYSTEM_WELCOME
}

data class AppNotification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.SYSTEM_WELCOME,
    val relatedId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
