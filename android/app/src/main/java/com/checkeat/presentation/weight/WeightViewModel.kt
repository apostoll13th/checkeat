package com.checkeat.presentation.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.local.entity.WeightEntry
import com.checkeat.data.repository.WeightRepository
import com.checkeat.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для отслеживания веса
 */
@HiltViewModel
class WeightViewModel @Inject constructor(
    private val weightRepository: WeightRepository
) : ViewModel() {

    // Последняя запись о весе
    val latestWeight: StateFlow<WeightEntry?> = weightRepository
        .getLatestEntryFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Все записи за последние 30 дней
    val recentEntries: StateFlow<UiState<List<WeightEntry>>> = weightRepository
        .getRecentEntriesFlow(30)
        .map<List<WeightEntry>, UiState<List<WeightEntry>>> { entries ->
            UiState.Success(entries)
        }
        .catch { e ->
            emit(UiState.Error(e.message ?: "Ошибка загрузки истории веса"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _addWeightState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val addWeightState: StateFlow<UiState<Long>> = _addWeightState.asStateFlow()

    /**
     * Добавить запись о весе
     */
    fun addWeight(weightKg: Float, note: String? = null) {
        viewModelScope.launch {
            _addWeightState.value = UiState.Loading

            val result = weightRepository.addWeightEntry(weightKg, note)
            result.fold(
                onSuccess = { id ->
                    _addWeightState.value = UiState.Success(id)
                },
                onFailure = { error ->
                    _addWeightState.value = UiState.Error(
                        error.message ?: "Ошибка добавления веса"
                    )
                }
            )
        }
    }

    /**
     * Удалить запись
     */
    fun deleteWeight(id: Int) {
        viewModelScope.launch {
            weightRepository.deleteWeightEntry(id)
        }
    }

    /**
     * Вычислить изменение веса
     */
    fun getWeightChange(): StateFlow<Float?> = recentEntries.map { state ->
        when (state) {
            is UiState.Success -> {
                val entries = state.data
                if (entries.size >= 2) {
                    val latest = entries.first().weightKg
                    val oldest = entries.last().weightKg
                    latest - oldest
                } else null
            }
            else -> null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Сбросить состояние добавления
     */
    fun resetAddState() {
        _addWeightState.value = UiState.Idle
    }
}
