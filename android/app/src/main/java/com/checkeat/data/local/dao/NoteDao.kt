package com.checkeat.data.local.dao

import androidx.room.*
import com.checkeat.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAll(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Int): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE analysisId = :analysisId")
    fun getByAnalysisIdFlow(analysisId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchNotesFlow(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
