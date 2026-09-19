package com.example.data.model

enum class RequestStatus(val displayName: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class SkillRequest(
    val requestId: String = "",
    val skillId: String = "",
    val skillTitle: String = "",
    val learnerId: String = "",
    val learnerName: String = "",
    val learnerPhoto: String = "",
    val mentorId: String = "",
    val mentorName: String = "",
    val mentorPhoto: String = "",
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val message: String = "",
    val pointsCost: Int = 100,
    val status: RequestStatus = RequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
