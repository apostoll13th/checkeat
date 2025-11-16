package com.checkeat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Запись о потреблении воды
 */
@Entity(tableName = "water_intake")
data class WaterIntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = 0,
    val amountMl: Int, // Количество воды в мл
    val date: Date = Date(), // Дата и время
    val synced: Boolean = false
)
