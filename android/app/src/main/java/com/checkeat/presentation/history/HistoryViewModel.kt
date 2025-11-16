package com.checkeat.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.local.entity.FoodAnalysisEntity
import com.checkeat.data.repository.FoodAnalysisRepository
import com.checkeat.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана истории
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: FoodAnalysisRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<UiState<List<FoodAnalysisEntity>>>(UiState.Loading)
    val historyState: StateFlow<UiState<List<FoodAnalysisEntity>>> = _historyState.asStateFlow()

    init {
        loadHistory()
    }

    /**
     * Загрузить историю
     */
    fun loadHistory() {
        viewModelScope.launch {
            try {
                repository.getAllAnalysesFlow().collect { analyses ->
                    if (analyses.isEmpty()) {
                        _historyState.value = UiState.Success(emptyList())
                    } else {
                        _historyState.value = UiState.Success(analyses)
                    }
                }
            } catch (e: Exception) {
                _historyState.value = UiState.Error(
                    e.message ?: "Ошибка загрузки истории"
                )
            }
        }
    }

    /**
     * Удалить анализ
     */
    fun deleteAnalysis(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteAnalysis(id)
                // История обновится автоматически через Flow
            } catch (e: Exception) {
                _historyState.value = UiState.Error(
                    e.message ?: "Ошибка удаления"
                )
            }
        }
    }

    /**
     * Синхронизировать с сервером
     */
    fun syncWithServer() {
        viewModelScope.launch {
            _historyState.value = UiState.Loading
            try {
                repository.syncHistory()
                // После синхронизации история обновится через Flow
            } catch (e: Exception) {
                _historyState.value = UiState.Error(
                    e.message ?: "Ошибка синхронизации"
                )
            }
        }
    }
}
