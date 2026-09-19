package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PointTransaction
import com.example.data.model.TransactionType

@Entity(tableName = "point_transactions")
data class PointTransactionEntity(
    @PrimaryKey val transactionId: String,
    val userId: String,
    val type: TransactionType,
    val amount: Int,
    val description: String,
    val sessionId: String?,
    val createdAt: Long
) {
    fun toModel(): PointTransaction = PointTransaction(
        transactionId = transactionId,
        userId = userId,
        type = type,
        amount = amount,
        description = description,
        sessionId = sessionId,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(tx: PointTransaction): PointTransactionEntity = PointTransactionEntity(
            transactionId = tx.transactionId,
            userId = tx.userId,
            type = tx.type,
            amount = tx.amount,
            description = tx.description,
            sessionId = tx.sessionId,
            createdAt = tx.createdAt
        )
    }
}
