package com.checkeat.data.repository

import com.checkeat.data.local.dao.NoteDao
import com.checkeat.data.local.entity.NoteEntity
import com.checkeat.data.remote.api.CheckEatApi
import com.checkeat.data.remote.api.NoteRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с заметками
 */
@Singleton
class NoteRepository @Inject constructor(
    private val api: CheckEatApi,
    private val localDao: NoteDao
) {

    /**
     * Получить все заметки (Flow)
     */
    fun getAllNotesFlow(): Flow<List<NoteEntity>> {
        return localDao.getAllFlow()
    }

    /**
     * Создать заметку
     */
    suspend fun createNote(
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        analysisId: Int? = null
    ): Result<Unit> {
        return try {
            val request = NoteRequest(title, content, tags, analysisId)
            val response = api.createNote(request)

            // Сохранить в локальную БД
            val entity = NoteEntity(
                id = response.id,
                userId = response.user_id,
                analysisId = response.analysis_id,
                title = response.title,
                content = response.content,
                tags = response.tags,
                createdAt = java.util.Date(),
                updatedAt = java.util.Date(),
                synced = true
            )
            localDao.insert(entity)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Обновить заметку
     */
    suspend fun updateNote(id: Int, title: String?, content: String?, tags: List<String>?): Result<Unit> {
        return try {
            api.updateNote(id, com.checkeat.data.remote.api.NoteUpdateRequest(title, content, tags))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Удалить заметку
     */
    suspend fun deleteNote(id: Int): Result<Unit> {
        return try {
            api.deleteNote(id)
            localDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Поиск заметок
     */
    fun searchNotes(query: String): Flow<List<NoteEntity>> {
        return localDao.searchNotesFlow(query)
    }
}
