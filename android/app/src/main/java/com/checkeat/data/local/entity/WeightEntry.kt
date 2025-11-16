package com.checkeat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Запись о весе тела
 */
@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = 0,
    val weightKg: Float, // Вес в килограммах
    val date: Date = Date(), // Дата и время взвешивания
    val note: String? = null, // Необязательная заметка
    val synced: Boolean = false
)
