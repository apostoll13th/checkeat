package com.checkeat.data.local.dao

import androidx.room.*
import com.checkeat.data.local.entity.DailyGoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с дневными целями
 */
@Dao
interface DailyGoalDao {

    @Query("SELECT * FROM daily_goals WHERE userId = :userId LIMIT 1")
    fun getGoalsFlow(userId: Int = 0): Flow<DailyGoalEntity?>

    @Query("SELECT * FROM daily_goals WHERE userId = :userId LIMIT 1")
    suspend fun getGoals(userId: Int = 0): DailyGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(goals: DailyGoalEntity)

    @Update
    suspend fun update(goals: DailyGoalEntity)

    @Delete
    suspend fun delete(goals: DailyGoalEntity)
}
