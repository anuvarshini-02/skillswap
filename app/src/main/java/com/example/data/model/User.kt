package com.example.data.model

data class User(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val bio: String = "",
    val location: String = "Campus / Online",
    val profileImage: String = "",
    val skillsTeaching: List<String> = emptyList(),
    val skillsLearning: List<String> = emptyList(),
    val skillPoints: Int = 200,
    val averageRating: Double = 5.0,
    val totalReviews: Int = 0,
    val completedSessions: Int = 0,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
