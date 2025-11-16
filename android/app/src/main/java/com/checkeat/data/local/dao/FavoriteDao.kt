package com.checkeat.data.local.dao

import androidx.room.*
import com.checkeat.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с избранными блюдами
 */
@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFlow(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): FavoriteEntity?

    @Query("SELECT * FROM favorites WHERE name LIKE '%' || :query || '%' ORDER BY addedAt DESC")
    fun searchFlow(query: String): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity): Long

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE name = :name LIMIT 1)")
    suspend fun existsByName(name: String): Boolean
}
