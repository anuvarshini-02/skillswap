package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.SessionMode
import com.example.data.model.Skill
import com.example.data.model.SkillLevel

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val skillId: String,
    val mentorId: String,
    val mentorName: String,
    val mentorPhoto: String,
    val mentorRating: Double,
    val mentorReviewsCount: Int,
    val title: String,
    val category: String,
    val description: String,
    val skillLevel: SkillLevel,
    val durationMinutes: Int,
    val pointsCost: Int,
    val mode: SessionMode,
    val learnOutcomes: List<String>,
    val prerequisites: String,
    val availableDays: List<String>,
    val availableTimes: String,
    val imageUrl: String,
    val isActive: Boolean,
    val isFavorite: Boolean,
    val createdAt: Long
) {
    fun toModel(): Skill = Skill(
        skillId = skillId,
        mentorId = mentorId,
        mentorName = mentorName,
        mentorPhoto = mentorPhoto,
        mentorRating = mentorRating,
        mentorReviewsCount = mentorReviewsCount,
        title = title,
        category = category,
        description = description,
        skillLevel = skillLevel,
        durationMinutes = durationMinutes,
        pointsCost = pointsCost,
        mode = mode,
        learnOutcomes = learnOutcomes,
        prerequisites = prerequisites,
        availableDays = availableDays,
        availableTimes = availableTimes,
        imageUrl = imageUrl,
        isActive = isActive,
        isFavorite = isFavorite,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(skill: Skill): SkillEntity = SkillEntity(
            skillId = skill.skillId,
            mentorId = skill.mentorId,
            mentorName = skill.mentorName,
            mentorPhoto = skill.mentorPhoto,
            mentorRating = skill.mentorRating,
            mentorReviewsCount = skill.mentorReviewsCount,
            title = skill.title,
            category = skill.category,
            description = skill.description,
            skillLevel = skill.skillLevel,
            durationMinutes = skill.durationMinutes,
            pointsCost = skill.pointsCost,
            mode = skill.mode,
            learnOutcomes = skill.learnOutcomes,
            prerequisites = skill.prerequisites,
            availableDays = skill.availableDays,
            availableTimes = skill.availableTimes,
            imageUrl = skill.imageUrl,
            isActive = skill.isActive,
            isFavorite = skill.isFavorite,
            createdAt = skill.createdAt
        )
    }
}
