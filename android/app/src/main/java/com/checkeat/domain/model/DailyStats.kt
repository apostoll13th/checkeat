package com.checkeat.domain.model

import java.util.Date

/**
 * Дневная статистика
 */
data class DailyStats(
    val date: Date,
    val totalCalories: Float,
    val totalProteins: Float,
    val totalFats: Float,
    val totalCarbs: Float,
    val mealsCount: Int
)

/**
 * Данные для графиков
 */
data class ChartData(
    val calorieData: List<CalorieDataPoint>,
    val macroData: List<MacroDataPoint>
)

data class CalorieDataPoint(
    val date: String,
    val calories: Float
)

data class MacroDataPoint(
    val date: String,
    val proteins: Float,
    val fats: Float,
    val carbs: Float
)
