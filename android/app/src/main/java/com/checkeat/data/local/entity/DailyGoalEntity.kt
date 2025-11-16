package com.checkeat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Дневные цели пользователя по питанию
 */
@Entity(tableName = "daily_goals")
data class DailyGoalEntity(
    @PrimaryKey
    val userId: Int = 0,
    val caloriesGoal: Float = 2000f, // Цель по калориям
    val proteinsGoal: Float = 150f,  // Цель по белкам (г)
    val fatsGoal: Float = 70f,       // Цель по жирам (г)
    val carbsGoal: Float = 250f,     // Цель по углеводам (г)
    val waterGoalMl: Int = 2000      // Цель по воде (мл)
)
