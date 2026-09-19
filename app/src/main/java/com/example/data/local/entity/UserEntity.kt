package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val bio: String,
    val location: String,
    val profileImage: String,
    val skillsTeaching: List<String>,
    val skillsLearning: List<String>,
    val skillPoints: Int,
    val averageRating: Double,
    val totalReviews: Int,
    val completedSessions: Int,
    val isAdmin: Boolean,
    val createdAt: Long
) {
    fun toModel(): User = User(
        userId = userId,
        fullName = fullName,
        email = email,
        phone = phone,
        bio = bio,
        location = location,
        profileImage = profileImage,
        skillsTeaching = skillsTeaching,
        skillsLearning = skillsLearning,
        skillPoints = skillPoints,
        averageRating = averageRating,
        totalReviews = totalReviews,
        completedSessions = completedSessions,
        isAdmin = isAdmin,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(user: User): UserEntity = UserEntity(
            userId = user.userId,
            fullName = user.fullName,
            email = user.email,
            phone = user.phone,
            bio = user.bio,
            location = user.location,
            profileImage = user.profileImage,
            skillsTeaching = user.skillsTeaching,
            skillsLearning = user.skillsLearning,
            skillPoints = user.skillPoints,
            averageRating = user.averageRating,
            totalReviews = user.totalReviews,
            completedSessions = user.completedSessions,
            isAdmin = user.isAdmin,
            createdAt = user.createdAt
        )
    }
}
