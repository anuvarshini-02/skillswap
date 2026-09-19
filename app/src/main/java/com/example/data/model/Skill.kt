package com.example.data.model

data class Skill(
    val skillId: String = "",
    val mentorId: String = "",
    val mentorName: String = "",
    val mentorPhoto: String = "",
    val mentorRating: Double = 5.0,
    val mentorReviewsCount: Int = 0,
    val title: String = "",
    val category: String = "Programming",
    val description: String = "",
    val skillLevel: SkillLevel = SkillLevel.BEGINNER,
    val durationMinutes: Int = 60,
    val pointsCost: Int = 100,
    val mode: SessionMode = SessionMode.ONLINE,
    val learnOutcomes: List<String> = emptyList(),
    val prerequisites: String = "None, beginners welcome!",
    val availableDays: List<String> = listOf("Mon", "Wed", "Fri", "Sat"),
    val availableTimes: String = "4:00 PM - 8:00 PM",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
