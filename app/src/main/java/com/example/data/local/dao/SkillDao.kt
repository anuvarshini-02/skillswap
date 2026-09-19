package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY createdAt DESC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE skillId = :skillId LIMIT 1")
    fun getSkillById(skillId: String): Flow<SkillEntity?>

    @Query("SELECT COUNT(*) FROM skills")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Query("UPDATE skills SET isActive = :isActive WHERE skillId = :skillId")
    suspend fun updateSkillActiveStatus(skillId: String, isActive: Boolean)

    @Query("DELETE FROM skills WHERE skillId = :skillId")
    suspend fun deleteSkillById(skillId: String)

    @Query("DELETE FROM skills")
    suspend fun clearAll()
}
