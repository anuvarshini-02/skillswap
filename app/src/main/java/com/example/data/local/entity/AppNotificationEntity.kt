package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey val notificationId: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedId: String,
    val timestamp: Long,
    val isRead: Boolean
) {
    fun toModel(): AppNotification = AppNotification(
        notificationId = notificationId,
        userId = userId,
        title = title,
        message = message,
        type = type,
        relatedId = relatedId,
        timestamp = timestamp,
        isRead = isRead
    )

    companion object {
        fun fromModel(notif: AppNotification): AppNotificationEntity = AppNotificationEntity(
            notificationId = notif.notificationId,
            userId = notif.userId,
            title = notif.title,
            message = notif.message,
            type = notif.type,
            relatedId = notif.relatedId,
            timestamp = notif.timestamp,
            isRead = notif.isRead
        )
    }
}
