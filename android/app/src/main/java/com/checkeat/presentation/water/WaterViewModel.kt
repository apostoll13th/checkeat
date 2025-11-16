package com.checkeat.presentation.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.local.entity.WaterIntakeEntity
import com.checkeat.data.repository.DailyGoalRepository
import com.checkeat.data.repository.WaterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для отслеживания водного баланса
 */
@HiltViewModel
class WaterViewModel @Inject constructor(
    private val waterRepository: WaterRepository,
    private val goalRepository: DailyGoalRepository
) : ViewModel() {

    // Текущее потребление воды за сегодня
    val todayWaterTotal: StateFlow<Int> = waterRepository
        .getTodayTotalFlow()
        .map { it ?: 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Цель по воде
    val waterGoal: StateFlow<Int> = goalRepository
        .getGoalsFlow()
        .map { it?.waterGoalMl ?: 2000 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 2000
        )

    // Прогресс в процентах
    val waterProgress: StateFlow<Float> = combine(
        todayWaterTotal,
        waterGoal
    ) { total, goal ->
        if (goal > 0) (total.toFloat() / goal.toFloat() * 100f).coerceIn(0f, 100f)
        else 0f
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    // История записей за сегодня
    val todayIntake: StateFlow<List<WaterIntakeEntity>> = waterRepository
        .getTodayIntakeFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Добавить воду
     */
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            waterRepository.addWaterIntake(amountMl)
        }
    }

    /**
     * Удалить запись
     */
    fun deleteWater(id: Int) {
        viewModelScope.launch {
            waterRepository.deleteWaterIntake(id)
        }
    }
}
