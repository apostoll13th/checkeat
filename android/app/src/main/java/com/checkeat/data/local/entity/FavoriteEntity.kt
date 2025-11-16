package com.checkeat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.checkeat.data.local.converter.Converters
import com.checkeat.domain.model.Dish
import com.checkeat.domain.model.Ingredient
import java.util.Date

/**
 * Избранное блюдо для быстрого добавления
 */
@Entity(tableName = "favorites")
@TypeConverters(Converters::class)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = 0,
    val name: String, // Название блюда
    val imageUrl: String? = null,
    val calories: Float,
    val proteins: Float,
    val fats: Float,
    val carbs: Float,
    val ingredients: List<Ingredient> = emptyList(),
    val dishes: List<Dish> = emptyList(),
    val addedAt: Date = Date()
)
