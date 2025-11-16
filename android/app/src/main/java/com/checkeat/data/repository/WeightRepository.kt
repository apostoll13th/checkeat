package com.checkeat.data.repository

import com.checkeat.data.local.dao.WeightDao
import com.checkeat.data.local.entity.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с весом тела
 */
@Singleton
class WeightRepository @Inject constructor(
    private val weightDao: WeightDao
) {

    /**
     * Получить все записи о весе
     */
    fun getAllEntriesFlow(limit: Int = 100): Flow<List<WeightEntry>> {
        return weightDao.getAllFlow(limit)
    }

    /**
     * Получить последнюю запись
     */
    fun getLatestEntryFlow(): Flow<WeightEntry?> {
        return weightDao.getLatestFlow()
    }

    /**
     * Получить записи за последние N дней
     */
    fun getRecentEntriesFlow(days: Int = 30): Flow<List<WeightEntry>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return weightDao.getEntriesSinceFlow(calendar.timeInMillis)
    }

    /**
     * Добавить запись о весе
     */
    suspend fun addWeightEntry(weightKg: Float, note: String? = null): Result<Long> {
        return try {
            val entry = WeightEntry(
                weightKg = weightKg,
                note = note,
                date = Date()
            )
            val id = weightDao.insert(entry)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Обновить запись
     */
    suspend fun updateWeightEntry(entry: WeightEntry): Result<Unit> {
        return try {
            weightDao.update(entry)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Удалить запись
     */
    suspend fun deleteWeightEntry(id: Int): Result<Unit> {
        return try {
            weightDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получить количество записей
     */
    suspend fun getEntriesCount(): Int {
        return weightDao.getCount()
    }
}
