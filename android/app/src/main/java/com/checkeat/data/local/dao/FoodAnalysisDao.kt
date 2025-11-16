package com.checkeat.data.local.dao

import androidx.room.*
import com.checkeat.data.local.entity.FoodAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodAnalysisDao {
    @Query("SELECT * FROM food_analyses ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<FoodAnalysisEntity>>

    @Query("SELECT * FROM food_analyses ORDER BY createdAt DESC")
    suspend fun getAll(): List<FoodAnalysisEntity>

    @Query("SELECT * FROM food_analyses WHERE id = :id")
    suspend fun getById(id: Int): FoodAnalysisEntity?

    @Query("SELECT * FROM food_analyses WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<FoodAnalysisEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analysis: FoodAnalysisEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(analyses: List<FoodAnalysisEntity>)

    @Update
    suspend fun update(analysis: FoodAnalysisEntity)

    @Delete
    suspend fun delete(analysis: FoodAnalysisEntity)

    @Query("DELETE FROM food_analyses WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM food_analyses")
    suspend fun deleteAll()

    @Query("SELECT * FROM food_analyses WHERE synced = 0")
    suspend fun getUnsyncedAnalyses(): List<FoodAnalysisEntity>
}
