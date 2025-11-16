package com.checkeat.data.local.dao

import androidx.room.*
import com.checkeat.data.local.entity.DailyTask
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с ежедневными задачами
 */
@Dao
interface DailyTaskDao {

    @Query("SELECT * FROM daily_tasks ORDER BY `order` ASC, id ASC")
    fun getAllFlow(): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE isCompleted = 1 ORDER BY `order` ASC")
    fun getCompletedFlow(): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE isCompleted = 0 ORDER BY `order` ASC")
    fun getIncompleteFlow(): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): DailyTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: DailyTask): Long

    @Update
    suspend fun update(task: DailyTask)

    @Delete
    suspend fun delete(task: DailyTask)

    @Query("DELETE FROM daily_tasks WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE daily_tasks SET isCompleted = :completed, completedDate = :date WHERE id = :id")
    suspend fun updateCompletionStatus(id: Int, completed: Boolean, date: Long?)

    /**
     * Сбросить все задачи (для начала нового дня)
     */
    @Query("UPDATE daily_tasks SET isCompleted = 0, completedDate = NULL WHERE isRecurring = 1")
    suspend fun resetRecurringTasks()
}
