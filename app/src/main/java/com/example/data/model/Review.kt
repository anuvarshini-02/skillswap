package com.example.data.model

data class Review(
    val reviewId: String = "",
    val sessionId: String = "",
    val skillId: String = "",
    val skillTitle: String = "",
    val mentorId: String = "",
    val learnerId: String = "",
    val learnerName: String = "Learner",
    val learnerPhoto: String = "",
    val rating: Float = 5.0f,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
