package com.checkeat.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.remote.api.DailyStatsDto
import com.checkeat.data.repository.StatsRepository
import com.checkeat.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана статистики
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: StatsRepository
) : ViewModel() {

    private val _dailyStatsState = MutableStateFlow<UiState<DailyStatsDto>>(UiState.Loading)
    val dailyStatsState: StateFlow<UiState<DailyStatsDto>> = _dailyStatsState.asStateFlow()

    private val _weeklyStatsState = MutableStateFlow<UiState<List<DailyStatsDto>>>(UiState.Idle)
    val weeklyStatsState: StateFlow<UiState<List<DailyStatsDto>>> = _weeklyStatsState.asStateFlow()

    init {
        loadDailyStats()
        loadWeeklyStats()
    }

    /**
     * Загрузить дневную статистику
     */
    fun loadDailyStats(date: String? = null) {
        viewModelScope.launch {
            _dailyStatsState.value = UiState.Loading
            try {
                val result = repository.getDailyStats(date)
                result.fold(
                    onSuccess = { stats ->
                        _dailyStatsState.value = UiState.Success(stats)
                    },
                    onFailure = { error ->
                        _dailyStatsState.value = UiState.Error(
                            error.message ?: "Ошибка загрузки статистики"
                        )
                    }
                )
            } catch (e: Exception) {
                _dailyStatsState.value = UiState.Error(
                    e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    /**
     * Загрузить недельную статистику
     */
    fun loadWeeklyStats() {
        viewModelScope.launch {
            _weeklyStatsState.value = UiState.Loading
            try {
                val result = repository.getWeeklyStats()
                result.fold(
                    onSuccess = { stats ->
                        _weeklyStatsState.value = UiState.Success(stats)
                    },
                    onFailure = { error ->
                        _weeklyStatsState.value = UiState.Error(
                            error.message ?: "Ошибка загрузки недельной статистики"
                        )
                    }
                )
            } catch (e: Exception) {
                _weeklyStatsState.value = UiState.Error(
                    e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    /**
     * Загрузить месячную статистику
     */
    fun loadMonthlyStats() {
        viewModelScope.launch {
            _weeklyStatsState.value = UiState.Loading
            try {
                val result = repository.getMonthlyStats()
                result.fold(
                    onSuccess = { stats ->
                        _weeklyStatsState.value = UiState.Success(stats)
                    },
                    onFailure = { error ->
                        _weeklyStatsState.value = UiState.Error(
                            error.message ?: "Ошибка загрузки месячной статистики"
                        )
                    }
                )
            } catch (e: Exception) {
                _weeklyStatsState.value = UiState.Error(
                    e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }
}
