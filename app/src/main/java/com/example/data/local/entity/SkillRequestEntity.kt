package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.RequestStatus
import com.example.data.model.SkillRequest

@Entity(tableName = "skill_requests")
data class SkillRequestEntity(
    @PrimaryKey val requestId: String,
    val skillId: String,
    val skillTitle: String,
    val learnerId: String,
    val learnerName: String,
    val learnerPhoto: String,
    val mentorId: String,
    val mentorName: String,
    val mentorPhoto: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val message: String,
    val pointsCost: Int,
    val status: RequestStatus,
    val createdAt: Long
) {
    fun toModel(): SkillRequest = SkillRequest(
        requestId = requestId,
        skillId = skillId,
        skillTitle = skillTitle,
        learnerId = learnerId,
        learnerName = learnerName,
        learnerPhoto = learnerPhoto,
        mentorId = mentorId,
        mentorName = mentorName,
        mentorPhoto = mentorPhoto,
        scheduledDate = scheduledDate,
        scheduledTime = scheduledTime,
        message = message,
        pointsCost = pointsCost,
        status = status,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(request: SkillRequest): SkillRequestEntity = SkillRequestEntity(
            requestId = request.requestId,
            skillId = request.skillId,
            skillTitle = request.skillTitle,
            learnerId = request.learnerId,
            learnerName = request.learnerName,
            learnerPhoto = request.learnerPhoto,
            mentorId = request.mentorId,
            mentorName = request.mentorName,
            mentorPhoto = request.mentorPhoto,
            scheduledDate = request.scheduledDate,
            scheduledTime = request.scheduledTime,
            message = request.message,
            pointsCost = request.pointsCost,
            status = request.status,
            createdAt = request.createdAt
        )
    }
}
