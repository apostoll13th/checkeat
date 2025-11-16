package com.checkeat.data.repository

import com.checkeat.data.remote.api.CheckEatApi
import com.checkeat.data.remote.api.ChartDataDto
import com.checkeat.data.remote.api.DailyStatsDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы со статистикой
 */
@Singleton
class StatsRepository @Inject constructor(
    private val api: CheckEatApi
) {

    /**
     * Получить дневную статистику
     */
    suspend fun getDailyStats(date: String? = null): Result<DailyStatsDto> {
        return try {
            val stats = api.getDailyStats(date)
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получить недельную статистику
     */
    suspend fun getWeeklyStats(): Result<List<DailyStatsDto>> {
        return try {
            val stats = api.getWeeklyStats()
            Result.success(stats.daily_stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получить месячную статистику
     */
    suspend fun getMonthlyStats(): Result<List<DailyStatsDto>> {
        return try {
            val stats = api.getMonthlyStats()
            Result.success(stats.daily_stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получить данные для графиков
     */
    suspend fun getChartData(days: Int = 7): Result<ChartDataDto> {
        return try {
            val data = api.getChartData(days)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
