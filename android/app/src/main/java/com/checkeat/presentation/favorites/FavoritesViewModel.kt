package com.checkeat.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.local.entity.FavoriteEntity
import com.checkeat.data.repository.FavoriteRepository
import com.checkeat.domain.model.Dish
import com.checkeat.domain.model.Ingredient
import com.checkeat.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления избранными блюдами
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Список избранных блюд
    val favoritesState: StateFlow<UiState<List<FavoriteEntity>>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                favoriteRepository.getAllFavoritesFlow()
            } else {
                favoriteRepository.searchFavoritesFlow(query)
            }
        }
        .map<List<FavoriteEntity>, UiState<List<FavoriteEntity>>> { favorites ->
            UiState.Success(favorites)
        }
        .catch { e ->
            emit(UiState.Error(e.message ?: "Ошибка загрузки избранного"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _addState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val addState: StateFlow<UiState<Long>> = _addState.asStateFlow()

    /**
     * Добавить в избранное
     */
    fun addToFavorites(
        name: String,
        imageUrl: String?,
        calories: Float,
        proteins: Float,
        fats: Float,
        carbs: Float,
        ingredients: List<Ingredient>,
        dishes: List<Dish>
    ) {
        viewModelScope.launch {
            _addState.value = UiState.Loading

            val result = favoriteRepository.addToFavorites(
                name = name,
                imageUrl = imageUrl,
                calories = calories,
                proteins = proteins,
                fats = fats,
                carbs = carbs,
                ingredients = ingredients,
                dishes = dishes
            )

            result.fold(
                onSuccess = { id ->
                    _addState.value = UiState.Success(id)
                },
                onFailure = { error ->
                    _addState.value = UiState.Error(
                        error.message ?: "Ошибка добавления в избранное"
                    )
                }
            )
        }
    }

    /**
     * Удалить из избранного
     */
    fun removeFromFavorites(id: Int) {
        viewModelScope.launch {
            favoriteRepository.removeFromFavorites(id)
        }
    }

    /**
     * Обновить поисковый запрос
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Очистить поиск
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }

    /**
     * Сбросить состояние добавления
     */
    fun resetAddState() {
        _addState.value = UiState.Idle
    }
}
