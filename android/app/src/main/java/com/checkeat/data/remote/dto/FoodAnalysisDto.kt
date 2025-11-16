package com.checkeat.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FoodAnalysisDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("calories")
    val calories: Float,
    @SerializedName("proteins")
    val proteins: Float,
    @SerializedName("fats")
    val fats: Float,
    @SerializedName("carbs")
    val carbs: Float,
    @SerializedName("ingredients_json")
    val ingredients: List<IngredientDto>,
    @SerializedName("health_tips_json")
    val healthTips: List<String>,
    @SerializedName("taste_tips_json")
    val tasteTips: List<String>,
    @SerializedName("dishes_json")
    val dishes: List<DishDto>,
    @SerializedName("created_at")
    val createdAt: String
)

data class DishDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("portion_g")
    val portionG: Float,
    @SerializedName("calories")
    val calories: Float,
    @SerializedName("proteins")
    val proteins: Float,
    @SerializedName("fats")
    val fats: Float,
    @SerializedName("carbs")
    val carbs: Float
)

data class IngredientDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("amount")
    val amount: String
)

data class FoodAnalysisListResponse(
    @SerializedName("items")
    val items: List<FoodAnalysisDto>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("page_size")
    val pageSize: Int,
    @SerializedName("pages")
    val pages: Int
)
