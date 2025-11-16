package com.checkeat.data.repository

import com.checkeat.data.local.dao.FavoriteDao
import com.checkeat.data.local.entity.FavoriteEntity
import com.checkeat.domain.model.Dish
import com.checkeat.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с избранными блюдами
 */
@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {

    /**
     * Получить все избранные блюда
     */
    fun getAllFavoritesFlow(): Flow<List<FavoriteEntity>> {
        return favoriteDao.getAllFlow()
    }

    /**
     * Поиск избранных блюд
     */
    fun searchFavoritesFlow(query: String): Flow<List<FavoriteEntity>> {
        return favoriteDao.searchFlow(query)
    }

    /**
     * Добавить в избранное
     */
    suspend fun addToFavorites(
        name: String,
        imageUrl: String?,
        calories: Float,
        proteins: Float,
        fats: Float,
        carbs: Float,
        ingredients: List<Ingredient>,
        dishes: List<Dish>
    ): Result<Long> {
        return try {
            // Проверяем, есть ли уже такое блюдо
            val exists = favoriteDao.existsByName(name)
            if (exists) {
                return Result.failure(Exception("Блюдо уже в избранном"))
            }

            val favorite = FavoriteEntity(
                name = name,
                imageUrl = imageUrl,
                calories = calories,
                proteins = proteins,
                fats = fats,
                carbs = carbs,
                ingredients = ingredients,
                dishes = dishes
            )
            val id = favoriteDao.insert(favorite)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Удалить из избранного
     */
    suspend fun removeFromFavorites(id: Int): Result<Unit> {
        return try {
            favoriteDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получить избранное по ID
     */
    suspend fun getFavoriteById(id: Int): FavoriteEntity? {
        return favoriteDao.getById(id)
    }
}
