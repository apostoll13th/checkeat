package com.checkeat.data.repository

import com.checkeat.data.local.dao.FoodAnalysisDao
import com.checkeat.data.local.entity.FoodAnalysisEntity
import com.checkeat.data.remote.api.CheckEatApi
import com.checkeat.data.remote.dto.FoodAnalysisDto
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с анализами еды
 */
@Singleton
class FoodAnalysisRepository @Inject constructor(
    private val api: CheckEatApi,
    private val localDao: FoodAnalysisDao
) {

    /**
     * Получить все анализы из локальной БД (Flow для автообновления UI)
     */
    fun getAllAnalysesFlow(): Flow<List<FoodAnalysisEntity>> {
        return localDao.getAllFlow()
    }

    /**
     * Получить анализ по ID
     */
    suspend fun getAnalysisById(id: Int): FoodAnalysisEntity? {
        return localDao.getById(id)
    }

    /**
     * Анализировать фото еды
     */
    suspend fun analyzeFood(photo: MultipartBody.Part): Result<FoodAnalysisDto> {
        return try {
            val result = api.analyzeFood(photo)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Синхронизировать историю с сервера
     */
    suspend fun syncHistory(): Result<Unit> {
        return try {
            val response = api.getHistory()
            // Здесь должна быть логика конвертации DTO в Entity и сохранения
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Удалить анализ
     */
    suspend fun deleteAnalysis(id: Int): Result<Unit> {
        return try {
            api.deleteAnalysis(id)
            localDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
