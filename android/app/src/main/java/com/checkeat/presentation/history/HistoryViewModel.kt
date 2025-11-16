package com.checkeat.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.local.entity.FoodAnalysisEntity
import com.checkeat.data.repository.FoodAnalysisRepository
import com.checkeat.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана истории с улучшенным управлением памятью
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: FoodAnalysisRepository
) : ViewModel() {

    // Улучшение: использование stateIn вместо бесконечного collect для предотвращения утечек памяти
    val historyState: StateFlow<UiState<List<FoodAnalysisEntity>>> = repository
        .getAllAnalysesFlow()
        .map<List<FoodAnalysisEntity>, UiState<List<FoodAnalysisEntity>>> { analyses ->
            UiState.Success(analyses)
        }
        .catch { e ->
            emit(UiState.Error(e.message ?: "Ошибка загрузки истории"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    /**
     * Удалить анализ
     */
    fun deleteAnalysis(id: Int) {
        viewModelScope.launch {
            repository.deleteAnalysis(id)
            // История обновится автоматически через Flow
        }
    }

    /**
     * Синхронизировать с сервером
     */
    fun syncWithServer() {
        viewModelScope.launch {
            repository.syncHistory()
            // После синхронизации история обновится автоматически через Flow
        }
    }
}
