package com.example.data.model

enum class SessionStatus(val displayName: String) {
    UPCOMING("Upcoming"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class Session(
    val sessionId: String = "",
    val requestId: String = "",
    val skillId: String = "",
    val skillTitle: String = "",
    val mentorId: String = "",
    val mentorName: String = "",
    val mentorPhoto: String = "",
    val learnerId: String = "",
    val learnerName: String = "",
    val learnerPhoto: String = "",
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val durationMinutes: Int = 60,
    val skillPoints: Int = 100,
    val status: SessionStatus = SessionStatus.UPCOMING,
    val mode: SessionMode = SessionMode.ONLINE,
    val meetingLinkOrLocation: String = "Google Meet / Campus Library Rm 302",
    val isReviewed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
