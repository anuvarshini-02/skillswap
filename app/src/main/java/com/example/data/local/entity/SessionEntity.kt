package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Session
import com.example.data.model.SessionMode
import com.example.data.model.SessionStatus

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val requestId: String,
    val skillId: String,
    val skillTitle: String,
    val mentorId: String,
    val mentorName: String,
    val mentorPhoto: String,
    val learnerId: String,
    val learnerName: String,
    val learnerPhoto: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val durationMinutes: Int,
    val skillPoints: Int,
    val status: SessionStatus,
    val mode: SessionMode,
    val meetingLinkOrLocation: String,
    val isReviewed: Boolean,
    val createdAt: Long,
    val completedAt: Long?
) {
    fun toModel(): Session = Session(
        sessionId = sessionId,
        requestId = requestId,
        skillId = skillId,
        skillTitle = skillTitle,
        mentorId = mentorId,
        mentorName = mentorName,
        mentorPhoto = mentorPhoto,
        learnerId = learnerId,
        learnerName = learnerName,
        learnerPhoto = learnerPhoto,
        scheduledDate = scheduledDate,
        scheduledTime = scheduledTime,
        durationMinutes = durationMinutes,
        skillPoints = skillPoints,
        status = status,
        mode = mode,
        meetingLinkOrLocation = meetingLinkOrLocation,
        isReviewed = isReviewed,
        createdAt = createdAt,
        completedAt = completedAt
    )

    companion object {
        fun fromModel(session: Session): SessionEntity = SessionEntity(
            sessionId = session.sessionId,
            requestId = session.requestId,
            skillId = session.skillId,
            skillTitle = session.skillTitle,
            mentorId = session.mentorId,
            mentorName = session.mentorName,
            mentorPhoto = session.mentorPhoto,
            learnerId = session.learnerId,
            learnerName = session.learnerName,
            learnerPhoto = session.learnerPhoto,
            scheduledDate = session.scheduledDate,
            scheduledTime = session.scheduledTime,
            durationMinutes = session.durationMinutes,
            skillPoints = session.skillPoints,
            status = session.status,
            mode = session.mode,
            meetingLinkOrLocation = session.meetingLinkOrLocation,
            isReviewed = session.isReviewed,
            createdAt = session.createdAt,
            completedAt = session.completedAt
        )
    }
}
