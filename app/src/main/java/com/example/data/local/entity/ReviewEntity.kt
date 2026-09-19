package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Review

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val reviewId: String,
    val sessionId: String,
    val skillId: String,
    val skillTitle: String,
    val mentorId: String,
    val learnerId: String,
    val learnerName: String,
    val learnerPhoto: String,
    val rating: Float,
    val comment: String,
    val createdAt: Long
) {
    fun toModel(): Review = Review(
        reviewId = reviewId,
        sessionId = sessionId,
        skillId = skillId,
        skillTitle = skillTitle,
        mentorId = mentorId,
        learnerId = learnerId,
        learnerName = learnerName,
        learnerPhoto = learnerPhoto,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(review: Review): ReviewEntity = ReviewEntity(
            reviewId = review.reviewId,
            sessionId = review.sessionId,
            skillId = review.skillId,
            skillTitle = review.skillTitle,
            mentorId = review.mentorId,
            learnerId = review.learnerId,
            learnerName = review.learnerName,
            learnerPhoto = review.learnerPhoto,
            rating = review.rating,
            comment = review.comment,
            createdAt = review.createdAt
        )
    }
}
