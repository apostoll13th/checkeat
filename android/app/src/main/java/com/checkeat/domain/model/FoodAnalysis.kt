package com.checkeat.domain.model

import java.util.Date

/**
 * Модель анализа еды
 */
data class FoodAnalysis(
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
    val createdAt: Date
)

data class Dish(
    val name: String,
    val portionG: Float,
    val calories: Float,
    val proteins: Float,
    val fats: Float,
    val carbs: Float
)

data class Ingredient(
    val name: String,
    val amount: String
)
