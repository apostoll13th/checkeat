package com.checkeat.data.local.converter

import androidx.room.TypeConverter
import com.checkeat.domain.model.Dish
import com.checkeat.domain.model.Ingredient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Type Converters для Room Database
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return value?.let { gson.fromJson(it, listType) } ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return gson.toJson(list ?: emptyList<String>())
    }

    @TypeConverter
    fun fromIngredientList(value: String?): List<Ingredient> {
        val listType = object : TypeToken<List<Ingredient>>() {}.type
        return value?.let { gson.fromJson(it, listType) } ?: emptyList()
    }

    @TypeConverter
    fun toIngredientList(list: List<Ingredient>?): String {
        return gson.toJson(list ?: emptyList<Ingredient>())
    }

    @TypeConverter
    fun fromDishList(value: String?): List<Dish> {
        val listType = object : TypeToken<List<Dish>>() {}.type
        return value?.let { gson.fromJson(it, listType) } ?: emptyList()
    }

    @TypeConverter
    fun toDishList(list: List<Dish>?): String {
        return gson.toJson(list ?: emptyList<Dish>())
    }
}
