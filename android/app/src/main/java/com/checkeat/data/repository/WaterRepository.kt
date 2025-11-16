package com.checkeat.data.repository

import com.checkeat.data.local.dao.WaterIntakeDao
import com.checkeat.data.local.entity.WaterIntakeEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с водным балансом
 */
@Singleton
class WaterRepository @Inject constructor(
    private val waterIntakeDao: WaterIntakeDao
) {

    /**
     * Получить записи о воде за сегодня
     */
    fun getTodayIntakeFlow(): Flow<List<WaterIntakeEntity>> {
        return waterIntakeDao.getTodayIntakeFlow()
    }

    /**
     * Получить общее количество воды за сегодня
     */
    fun getTodayTotalFlow(): Flow<Int?> {
        return waterIntakeDao.getTodayTotalFlow()
    }

    /**
     * Добавить запись о потреблении воды
     */
    suspend fun addWaterIntake(amountMl: Int): Result<Unit> {
        return try {
            val waterIntake = WaterIntakeEntity(
                amountMl = amountMl,
                date = Date()
            )
            waterIntakeDao.insert(waterIntake)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Удалить запись
     */
    suspend fun deleteWaterIntake(id: Int): Result<Unit> {
        return try {
            waterIntakeDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получить последние записи
     */
    fun getRecentIntakeFlow(limit: Int = 100): Flow<List<WaterIntakeEntity>> {
        return waterIntakeDao.getRecentFlow(limit)
    }
}
