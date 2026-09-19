package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.PointTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointTransactionDao {
    @Query("SELECT * FROM point_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<PointTransactionEntity>>

    @Query("SELECT * FROM point_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsByUserId(userId: String): Flow<List<PointTransactionEntity>>

    @Query("SELECT COUNT(*) FROM point_transactions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PointTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<PointTransactionEntity>)
}
