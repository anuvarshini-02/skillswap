package com.example.data.remote

import com.example.data.model.SessionMode
import com.example.data.model.Skill
import com.example.data.model.SkillLevel

data class RemoteSkillDto(
    val id: String = "",
    val title: String = "",
    val category: String = "Programming",
    val description: String = "",
    val mentorName: String = "",
    val mentorPhoto: String = "",
    val mentorRating: Double = 4.9,
    val durationMinutes: Int = 60,
    val pointsCost: Int = 120,
    val mode: String = "ONLINE",
    val skillLevel: String = "INTERMEDIATE",
    val learnOutcomes: List<String> = emptyList(),
    val prerequisites: String = "Basic fundamentals",
    val availableDays: List<String> = listOf("Tue", "Thu", "Sat"),
    val availableTimes: String = "5:00 PM - 8:00 PM",
    val imageUrl: String = ""
) {
    fun toSkill(): Skill {
        val level = try {
            SkillLevel.valueOf(skillLevel.uppercase())
        } catch (_: Exception) {
            SkillLevel.INTERMEDIATE
        }

        val sessionMode = try {
            SessionMode.valueOf(mode.uppercase())
        } catch (_: Exception) {
            SessionMode.ONLINE
        }

        return Skill(
            skillId = if (id.isNotBlank()) "api_$id" else "api_${System.currentTimeMillis()}",
            mentorId = "remote_mentor_${id.take(6)}",
            mentorName = mentorName.ifBlank { "Community Educator" },
            mentorPhoto = mentorPhoto.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80" },
            mentorRating = mentorRating,
            mentorReviewsCount = 12,
            title = title,
            category = category,
            description = description,
            skillLevel = level,
            durationMinutes = durationMinutes,
            pointsCost = pointsCost,
            mode = sessionMode,
            learnOutcomes = learnOutcomes,
            prerequisites = prerequisites,
            availableDays = availableDays,
            availableTimes = availableTimes,
            imageUrl = imageUrl,
            isActive = true,
            isFavorite = false,
            createdAt = System.currentTimeMillis()
        )
    }
}

data class ApiStatusResponse(
    val status: String,
    val message: String,
    val serverTime: Long = System.currentTimeMillis()
)
