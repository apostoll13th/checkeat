package com.checkeat.data.repository

import com.checkeat.data.local.dao.DailyGoalDao
import com.checkeat.data.local.entity.DailyGoalEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с дневными целями
 */
@Singleton
class DailyGoalRepository @Inject constructor(
    private val dailyGoalDao: DailyGoalDao
) {

    /**
     * Получить цели пользователя
     */
    fun getGoalsFlow(userId: Int = 0): Flow<DailyGoalEntity?> {
        return dailyGoalDao.getGoalsFlow(userId)
    }

    /**
     * Получить цели (один раз)
     */
    suspend fun getGoals(userId: Int = 0): DailyGoalEntity? {
        return dailyGoalDao.getGoals(userId)
    }

    /**
     * Установить или обновить цели
     */
    suspend fun updateGoals(
        caloriesGoal: Float,
        proteinsGoal: Float,
        fatsGoal: Float,
        carbsGoal: Float,
        waterGoalMl: Int,
        userId: Int = 0
    ): Result<Unit> {
        return try {
            val goals = DailyGoalEntity(
                userId = userId,
                caloriesGoal = caloriesGoal,
                proteinsGoal = proteinsGoal,
                fatsGoal = fatsGoal,
                carbsGoal = carbsGoal,
                waterGoalMl = waterGoalMl
            )
            dailyGoalDao.insertOrUpdate(goals)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Создать дефолтные цели, если их нет
     */
    suspend fun ensureDefaultGoals(userId: Int = 0): Result<Unit> {
        return try {
            val existing = dailyGoalDao.getGoals(userId)
            if (existing == null) {
                dailyGoalDao.insertOrUpdate(DailyGoalEntity(userId = userId))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
