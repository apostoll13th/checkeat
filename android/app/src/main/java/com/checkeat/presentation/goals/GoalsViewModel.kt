package com.checkeat.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.local.entity.DailyGoalEntity
import com.checkeat.data.repository.DailyGoalRepository
import com.checkeat.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления дневными целями
 */
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: DailyGoalRepository
) : ViewModel() {

    init {
        // Создаем дефолтные цели при первом запуске
        viewModelScope.launch {
            goalRepository.ensureDefaultGoals()
        }
    }

    // Текущие цели
    val goalsState: StateFlow<UiState<DailyGoalEntity>> = goalRepository
        .getGoalsFlow()
        .map { goals ->
            if (goals != null) {
                UiState.Success(goals)
            } else {
                UiState.Error("Цели не установлены")
            }
        }
        .catch { e ->
            emit(UiState.Error(e.message ?: "Ошибка загрузки целей"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _updateState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val updateState: StateFlow<UiState<Unit>> = _updateState.asStateFlow()

    /**
     * Обновить цели
     */
    fun updateGoals(
        caloriesGoal: Float,
        proteinsGoal: Float,
        fatsGoal: Float,
        carbsGoal: Float,
        waterGoalMl: Int
    ) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading

            val result = goalRepository.updateGoals(
                caloriesGoal = caloriesGoal,
                proteinsGoal = proteinsGoal,
                fatsGoal = fatsGoal,
                carbsGoal = carbsGoal,
                waterGoalMl = waterGoalMl
            )

            result.fold(
                onSuccess = {
                    _updateState.value = UiState.Success(Unit)
                },
                onFailure = { error ->
                    _updateState.value = UiState.Error(
                        error.message ?: "Ошибка обновления целей"
                    )
                }
            )
        }
    }

    /**
     * Сбросить состояние обновления
     */
    fun resetUpdateState() {
        _updateState.value = UiState.Idle
    }
}
