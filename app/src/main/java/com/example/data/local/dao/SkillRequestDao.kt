package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.SkillRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillRequestDao {
    @Query("SELECT * FROM skill_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<SkillRequestEntity>>

    @Query("SELECT COUNT(*) FROM skill_requests")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: SkillRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<SkillRequestEntity>)

    @Update
    suspend fun updateRequest(request: SkillRequestEntity)

    @Query("DELETE FROM skill_requests WHERE requestId = :requestId")
    suspend fun deleteRequest(requestId: String)
}
