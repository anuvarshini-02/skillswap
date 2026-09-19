package com.example.data.model

enum class TransactionType(val displayName: String, val isPositive: Boolean) {
    WELCOME_BONUS("Welcome Bonus", true),
    EARNED_TEACHING("Points Earned (Taught Session)", true),
    SPENT_LEARNING("Points Spent (Learning Session)", false),
    REFUND("Points Refunded (Cancelled Session)", true),
    ADMIN_BONUS("Admin Bonus", true)
}

data class PointTransaction(
    val transactionId: String = "",
    val userId: String = "",
    val type: TransactionType = TransactionType.WELCOME_BONUS,
    val amount: Int = 0,
    val description: String = "",
    val sessionId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
