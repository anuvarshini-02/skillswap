package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(";;;") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(";;;").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromSkillLevel(level: SkillLevel?): String {
        return level?.name ?: SkillLevel.BEGINNER.name
    }

    @TypeConverter
    fun toSkillLevel(value: String?): SkillLevel {
        return try {
            if (value.isNullOrBlank()) SkillLevel.BEGINNER else SkillLevel.valueOf(value)
        } catch (_: Exception) {
            SkillLevel.BEGINNER
        }
    }

    @TypeConverter
    fun fromSessionMode(mode: SessionMode?): String {
        return mode?.name ?: SessionMode.ONLINE.name
    }

    @TypeConverter
    fun toSessionMode(value: String?): SessionMode {
        return try {
            if (value.isNullOrBlank()) SessionMode.ONLINE else SessionMode.valueOf(value)
        } catch (_: Exception) {
            SessionMode.ONLINE
        }
    }

    @TypeConverter
    fun fromSessionStatus(status: SessionStatus?): String {
        return status?.name ?: SessionStatus.UPCOMING.name
    }

    @TypeConverter
    fun toSessionStatus(value: String?): SessionStatus {
        return try {
            if (value.isNullOrBlank()) SessionStatus.UPCOMING else SessionStatus.valueOf(value)
        } catch (_: Exception) {
            SessionStatus.UPCOMING
        }
    }

    @TypeConverter
    fun fromRequestStatus(status: RequestStatus?): String {
        return status?.name ?: RequestStatus.PENDING.name
    }

    @TypeConverter
    fun toRequestStatus(value: String?): RequestStatus {
        return try {
            if (value.isNullOrBlank()) RequestStatus.PENDING else RequestStatus.valueOf(value)
        } catch (_: Exception) {
            RequestStatus.PENDING
        }
    }

    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String {
        return type?.name ?: TransactionType.WELCOME_BONUS.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType {
        return try {
            if (value.isNullOrBlank()) TransactionType.WELCOME_BONUS else TransactionType.valueOf(value)
        } catch (_: Exception) {
            TransactionType.WELCOME_BONUS
        }
    }

    @TypeConverter
    fun fromNotificationType(type: NotificationType?): String {
        return type?.name ?: NotificationType.SYSTEM_WELCOME.name
    }

    @TypeConverter
    fun toNotificationType(value: String?): NotificationType {
        return try {
            if (value.isNullOrBlank()) NotificationType.SYSTEM_WELCOME else NotificationType.valueOf(value)
        } catch (_: Exception) {
            NotificationType.SYSTEM_WELCOME
        }
    }
}
