package com.checkeat.data.local.dao

import androidx.room.*
import com.checkeat.data.local.entity.WeightEntry
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с записями о весе
 */
@Dao
interface WeightDao {

    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT :limit")
    fun getAllFlow(limit: Int = 100): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT 1")
    fun getLatestFlow(): Flow<WeightEntry?>

    @Query("SELECT * FROM weight_entries WHERE DATE(date/1000, 'unixepoch') >= DATE(:startDate/1000, 'unixepoch') ORDER BY date ASC")
    fun getEntriesSinceFlow(startDate: Long): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): WeightEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightEntry): Long

    @Update
    suspend fun update(entry: WeightEntry)

    @Delete
    suspend fun delete(entry: WeightEntry)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM weight_entries")
    suspend fun getCount(): Int
}
