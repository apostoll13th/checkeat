package com.checkeat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.checkeat.data.local.converter.Converters
import com.checkeat.domain.model.Dish
import com.checkeat.domain.model.Ingredient
import java.util.Date

@Entity(tableName = "food_analyses")
@TypeConverters(Converters::class)
data class FoodAnalysisEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val imageUrl: String,
    val calories: Float,
    val proteins: Float,
    val fats: Float,
    val carbs: Float,
    val ingredients: List<Ingredient>,
    val healthTips: List<String>,
    val tasteTips: List<String>,
    val dishes: List<Dish>,
    val createdAt: Date,
    val synced: Boolean = true
)
