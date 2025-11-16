package com.checkeat.data.local.dao

import androidx.room.*
import com.checkeat.data.local.entity.WaterIntakeEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * DAO для работы с записями о потреблении воды
 */
@Dao
interface WaterIntakeDao {

    @Query("SELECT * FROM water_intake WHERE DATE(date/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch') ORDER BY date DESC")
    fun getTodayIntakeFlow(date: Long = System.currentTimeMillis()): Flow<List<WaterIntakeEntity>>

    @Query("SELECT SUM(amountMl) FROM water_intake WHERE DATE(date/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')")
    fun getTodayTotalFlow(date: Long = System.currentTimeMillis()): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(waterIntake: WaterIntakeEntity)

    @Delete
    suspend fun delete(waterIntake: WaterIntakeEntity)

    @Query("DELETE FROM water_intake WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM water_intake ORDER BY date DESC LIMIT :limit")
    fun getRecentFlow(limit: Int = 100): Flow<List<WaterIntakeEntity>>
}
