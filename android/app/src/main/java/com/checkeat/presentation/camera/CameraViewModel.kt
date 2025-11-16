package com.checkeat.presentation.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.repository.FoodAnalysisRepository
import com.checkeat.domain.model.FoodAnalysis
import com.checkeat.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

/**
 * ViewModel для экрана камеры
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: FoodAnalysisRepository
) : ViewModel() {

    private val _analysisState = MutableStateFlow<UiState<FoodAnalysis>>(UiState.Idle)
    val analysisState: StateFlow<UiState<FoodAnalysis>> = _analysisState.asStateFlow()

    /**
     * Анализировать фото еды
     */
    fun analyzeFood(imageFile: File) {
        viewModelScope.launch {
            _analysisState.value = UiState.Loading

            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

                val result = repository.analyzeFood(body)

                result.fold(
                    onSuccess = { dto ->
                        // Здесь нужно конвертировать DTO в доменную модель
                        // Для упрощения используем временную заглушку
                        _analysisState.value = UiState.Success(
                            FoodAnalysis(
                                id = dto.id,
                                userId = dto.userId,
                                imageUrl = dto.imageUrl,
                                calories = dto.calories,
                                proteins = dto.proteins,
                                fats = dto.fats,
                                carbs = dto.carbs,
                                ingredients = dto.ingredients.map {
                                    com.checkeat.domain.model.Ingredient(it.name, it.amount)
                                },
                                healthTips = dto.healthTips,
                                tasteTips = dto.tasteTips,
                                dishes = dto.dishes.map {
                                    com.checkeat.domain.model.Dish(
                                        it.name, it.portionG, it.calories,
                                        it.proteins, it.fats, it.carbs
                                    )
                                },
                                createdAt = java.util.Date()
                            )
                        )
                    },
                    onFailure = { error ->
                        _analysisState.value = UiState.Error(
                            error.message ?: "Ошибка при анализе фото"
                        )
                    }
                )
            } catch (e: Exception) {
                _analysisState.value = UiState.Error(
                    e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    /**
     * Сбросить состояние
     */
    fun resetState() {
        _analysisState.value = UiState.Idle
    }
}
