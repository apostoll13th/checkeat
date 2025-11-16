package com.checkeat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Ежедневная задача (ToDo) для формирования здоровых привычек
 */
@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = 0,
    val title: String, // Название задачи (например, "Выпить 2л воды")
    val description: String? = null, // Описание
    val isCompleted: Boolean = false, // Выполнена ли сегодня
    val completedDate: Date? = null, // Дата последнего выполнения
    val isRecurring: Boolean = true, // Повторяющаяся каждый день
    val order: Int = 0, // Порядок отображения
    val createdAt: Date = Date()
)
